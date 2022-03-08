# Large Models of Source Code
I occasionally train and publicly release large neural language models on programs. Here, I describe how to use these.

## Availability & Usage
Note (01/28, 2022): these instructions have been updated to make it easier to swap out different pre-trained models in the same base Docker image. New models are being pushed over the next few days.

### Via DockerHub (Recommended)
A *base* Docker image containing a slightly modified version of the [gpt-neox repository](https://github.com/EleutherAI/gpt-neox) is [available via DockerHub](https://hub.docker.com/repository/docker/vhellendoorn/code-lms-neox). This image can be used together with a checkpoint file hosted on this [public Google Drive folder](https://drive.google.com/drive/folders/1Y5jRdLJmP6bJbJTadbylmvfQKAZuLC6p?usp=sharing). The base image size is 5.4GB, and the model checkpoints range up to 6GB, which is also the amount of GPU memory they require to run (running on CPU is neither tested nor recommended).

Download and untar a checkpoint file to a directory called `checkpoints/`. Then, start the container with the following commands (substituting another GPU device index if needed):
```
nvidia-docker run --rm -it -e NVIDIA_VISIBLE_DEVICES=0 --shm-size=1g --ulimit memlock=-1 --mount type=bind,src=$PWD/checkpoints,dst=/gpt-neox/checkpoints vhellendoorn/code-lms-neox
sudo ./deepy.py generate.py configs/text_generation.yml checkpoints/configs/local_setup.yml checkpoints/configs/2-7B.yml
```
**Note:** if not using the 2-7B parameter model, replace the final config file with the appropriate model size (e.g., `small` = 160M parameters, `medium` = 405M).

### Usage
Once the container is up, you can feed it an example such as `def return1():\n  """Returns 1."""\n  ` (note the whitespace tokens) and watch it predict `return 1` (and then probably a bunch of other `returnX` methods, depending on the sample).

The modifications to gpt-neox mentioned above center around the need to allow tabs and newlines in the prompt input. For the _interactive_ mode, these can be added using their escaped versions (`\t`, `\n`); when using file-based input, the project will read the entire file instead of treating each line as a prompt. By default, the command below will create an interactive prompt and return relatively short outputs (256 tokens) with a sampling temperature of 0.5; this behavior can be changed in `/gpt-neox/checkpoints/configs/text_generation.yml`.

A lower temperature (e.g., 0.2) will produce more consistent and plausible (to the model) predictions; a higher temperature such as the default may be useful for generating and evaluating many candidates (see the [Codex paper](https://arxiv.org/pdf/2107.03374) for recommendations). For the latter setting, consider switching to the `input-file` mode and providing an entire snippet (without escaping whitespace) in the corresponding file

## Models Trained on a 249GB Multi-lingual Corpus
Several models have been trained on a [large corpus](#data-characteristics) of code spanning 11 programming languages. This includes a 2.7B parameter model (trained for 100K and 150K steps), a 405M parameter model (100K steps) and a 160M parameter model (coming soon).

### Available Models
All models are available [at a public Google Drive folder](https://drive.google.com/drive/folders/1Y5jRdLJmP6bJbJTadbylmvfQKAZuLC6p?usp=sharing), in the form of Tar files with fairly self-explanatory names (e.g., 2-7B-100K => a 2.7B parameter model trained for 100K steps). Currently available models include:

* **GPT2 - 2.7B:** A 32 layer, 2,560 dimensional Transformer model, trained with a batch size of 128 sequences (256K tokens). Models available both at 100K and at 150K steps steps.
  * Note that GPT-Neox' [default config](https://github.com/EleutherAI/gpt-neox/blob/main/configs/2-7B.yml) for this model was modified to reduce the number of training steps (and learning rate decay steps accordingly) to 160K, down from 320K, to better match the available training resources. Hence, this model may not have reached its peak performance.
* **GPT2 - 0.4B:** A 24 layer, 1,024 dimensional Transformer model based on the [`medium` config](https://github.com/EleutherAI/gpt-neox/blob/main/configs/medium.yml), trained with 256K tokens per batch.
* **GPT2 - 160M (Training in progress!):** A 12 layer, 768 dimensional Transformer model based on the [`small` config](https://github.com/EleutherAI/gpt-neox/blob/main/configs/small.yml), trained with 256K tokens per batch.

### Data Characteristics
The final, filtered dataset size is as follows:

|Language|Repositories|Size(GB)|Files|
|------|-----|-----|-------|
|C | 10,749 | 55G | 3,037,112 |
|C# | 9,511 | 21G | 2,514,494 |
|C++ | 13,726 | 52G | 4,289,506 |
|Go | 12,371 | 15G | 1,416,789 |
|Java | 15,044 | 41G | 5,120,129 |
|JavaScript | 25,144 | 22G | 1,774,174 |
|PHP | 9,960 | 13G | 1,714,058 |
|Python | 25,446 | 16G | 1,550,208 |
|Ruby | 5,826 | 4.1G | 674,343 |
|Rust | 4,991 | 3.5G | 304,842 |
|Scala | 1,497 | 1.8G | 245,100 |
|TypeScript | 12,830 | 9.2G | 1,441,926 |

### Data Collectioon & Filtering
I cloned the most popular repositories for 11 popular programming languages with at least 50 stars (stopping at ~25K per langauge) from GitHub in October 2021. For each project, each file belonging to the majority-language of that project was extracted, yielding the training set below (after cleaning). This initial, unfiltered dataset spanned 631GB and 38.9M files.

Next, similar to Codex and CodeParrot, very large (>1MB) and very short (<100 tokens) files were filtered out, reducing the dataset to 424GB. Files were then deduplicated based on a hash of their content, which reduced the number of files by another 30% or so, leaving 249GB of data and 24.1M files. No tokenization filters were applied; the model processes entire files including all comments. A code-specific vocabulary was constructed on a random 5% subset of the files above.

### Training Process
Training was done on 4 to 8 NVIDIA RTX 8000 GPUs, largely following the standard config values, except also enabling "scaled-upper-triang-masked-softmax-fusion" and "bias-gelu-fusion" for performance and slightly changing the batch size (see [model details](#available-models)), data split (changed to 98.9%, 0.1%, 1%), initial loss scale (2^16), and print/eval intervals.

The below image shows the loss curve of the various models' training process in terms of validation loss.
![image](https://user-images.githubusercontent.com/1426353/151620489-5dbf5baf-2260-4143-8f17-12d469cccac0.png)
This image will be updated once 150K step training runs complete for the medium and small models.

### Caveats
The trained models come with a few minor known limitations:
- This model was not trained to solve programming problems, and may not perform well on a benchmark such as [HumanEval](https://github.com/openai/human-eval). Models like Codex (powering Copilot) are pretrained on natural language, which may boost their ability to interpret NL prompts; this model only learned language from comments in code.
- The model appears to start generating a random new file once it reaches the (predicted) end of the current one. It is possible that the end-of-document token was not properly added to the training data.
- Whitespace is **very important** to the model, since no preprocessing was done on the input files. For instance, the following snippet will yield poor predictions, because in Java we would never expect an instance-method at the top-level, as is indicated by the single level of (`\t`) indentation of the two lines within this method:
```
public int getTotalWeight(List<Integer> weights) {\n\t// Sum weights in parallel.\n\treturn 
```
Adjusting the indentation makes it predict more reasonable continuations:
```
public int getTotalWeight(List<Integer> weights) {\n\t\t// Sum weights in parallel.\n\t\treturn 
```
The Codex model discusses controlling for this to increase usability; this may be worth doing in a future version of the model.

## Evaluation

### Evaluating Codex
To download the test sets that we used in the paper (12 programming languages), use:
```
wget https://zenodo.org/record/6338015/files/unseen_test_sets.tar.gz
tar -xvzf unseen_test_sets.tar.gz
```

and then:
```
export OPENAI_API_KEY=<YOUR OPEN AI API KEY>
python3 -u Evaluation/eval_codex_all.py --dirs Code-sampled100
```

Where `<YOUR OPEN AI API KEY>` is a private string that can be obtained by signing up for[OpenAI's beta](https://beta.openai.com/account/api-keys).

As of **March 2022**, getting an API Key is free for 3 months, and afterwards a credit card is need to be entered. However, even after entering a credit card, using our evaluation script does not lead to any costs.

## Citation

[A Systematic Evaluation of Large Language Models of Code](https://arxiv.org/pdf/2202.13169)

```
@article{xu2022systematic,
  title={A Systematic Evaluation of Large Language Models of Code},
  author={Xu, Frank F and Alon, Uri and Neubig, Graham and Hellendoorn, Vincent J},
  journal={arXiv preprint arXiv:2202.13169},
  year={2022}
}
```