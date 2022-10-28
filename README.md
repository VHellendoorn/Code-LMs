# Large Models of Source Code
I occasionally train and publicly release large neural language models on programs, including [PolyCoder](https://arxiv.org/pdf/2202.13169.pdf). Here, I describe how to use these.

## October 2022 - PolyCoder is available on Huggingface!
Thanks to [@NinedayWang](https://github.com/NinedayWang), PolyCoder is available on the Huggingface Hub!

The available models are:
* `NinedayWang/PolyCoder-160M`
* `NinedayWang/PolyCoder-0.4B`
* `NinedayWang/PolyCoder-2.7B`

To use in Huggingface, simply run (requires the newest version of `transformers`: `pip install transformers==4.23.0` ):
```python
from transformers import AutoTokenizer, AutoModelForCausalLM

from packaging import version
assert version.parse(transformers.__version__) >= version.parse("4.23.0")

tokenizer = AutoTokenizer.from_pretrained("NinedayWang/PolyCoder-2.7B")
model = AutoModelForCausalLM.from_pretrained("NinedayWang/PolyCoder-2.7B")
```

The model can be used, for example, by:
```python
prompt = '''def binarySearch(arr, left, right, x):
    mid = (left +'''
input_ids = tokenizer.encode(prompt, return_tensors='pt')
result = model.generate(input_ids, max_length=50, num_beams=4, num_return_sequences=4)
for res in result:
    print(tokenizer.decode(res))
```

## Table of Contents
1. [Setup](#getting-started)
2. [Models (incl. PolyCoder)](#models)
3. [Datasets](#datasets)
4. [Evaluation](#evaluation)
5. [How to cite](#citation)


## Getting Started
All current models were trained using the [GPT NeoX toolkit](https://github.com/EleutherAI/gpt-neox). First, download a pretrained checkpoint as described below and then use this either with [a Docker image](#via-docker) or through our fork of this toolkit [from source](#from-source) to [generate code](#code-generation) or [replicate our evaluation](#evaluation).

### Retrieving Checkpoints
Checkpoint files for training PolyCoder are hosted on this [public Zenodo repository](https://zenodo.org/record/6363556). See [this section](#models) for details on currently available models. Model checkpoints range up to 6GB, which is also the amount of GPU memory they require to run (running on CPU is neither tested nor recommended). Download and untar a checkpoint file (in this case for a 2.7B parameter model trained for 150K steps) to a directory called `checkpoints/`, using:

```
mkdir checkpoints
cd checkpoints
wget https://zenodo.org/record/6363556/files/2-7B-150K.tar
tar -xvf 2-7B-150K.tar
```

### From Source
We maintain a public fork of the NeoX repository [here](https://github.com/frankxu2004/gpt-neox), which includes the (minor) changes we made to the codebase to allow for tabs & newlines in the tokenization, and also includes instructions for running the perplexity and HumanEval tasks. Note that this repository uses [a forked version](https://github.com/frankxu2004/lm-evaluation-harness) of the LM Evaluation Harness with the code benchmark from [our work](#citation).

Building this repository should match the process for GPT-NeoX almost exactly. You may also use the Docker image mentioned next, but mounting a checkout of the latest version of this fork over the `/gpt-neox` directory inside the container. Once set up `generate.py` entrypoint (described [below](#code-generation)) for free-form code generation, or use one of the commands [here](https://github.com/frankxu2004/gpt-neox#a-modified-version-for-polycoder-code-pretraining) to calculate perplexity and HumanEval results as in [the paper](https://arxiv.org/pdf/2202.13169).

### Via Docker
A *base* Docker image containing a slightly modified version of the [gpt-neox repository](https://github.com/EleutherAI/gpt-neox) is [available via DockerHub](https://hub.docker.com/repository/docker/vhellendoorn/code-lms-neox):
```
docker pull vhellendoorn/code-lms-neox:base
```

This image can be used together with a checkpoint file hosted on this [public Zenodo repository](https://zenodo.org/record/6363556). The base Docker image size is 5.4GB. Once a checkpoint has been retrieved, start the container with the following commands (substituting another GPU device index if needed):
```
nvidia-docker run --rm -it -e NVIDIA_VISIBLE_DEVICES=0 --shm-size=1g --ulimit memlock=-1 --mount type=bind,src=$PWD/checkpoints,dst=/gpt-neox/checkpoints vhellendoorn/code-lms-neox:base
```

### Code Generation
The following command can be used to generate code from a prompt:
```
sudo ./deepy.py generate.py configs/text_generation.yml checkpoints/configs/local_setup.yml checkpoints/configs/2-7B.yml
```
**Note:** if not using the 2.7B parameter model, replace the final config file with the appropriate model size (e.g., `small` = 160M parameters, `medium` = 405M).

Once the checkpoint has been loaded, you can feed it an example such as `def return1():\n  """Returns 1."""\n  ` (note the whitespace tokens) and watch it predict `return 1` (and then probably a bunch of other `returnX` methods, depending on the sample).

The modifications to gpt-neox mentioned above center around the need to allow tabs and newlines in the prompt input. For the _interactive_ mode, these can be added using their escaped versions (`\t`, `\n`); when using file-based input, the project will read the entire file instead of treating each line as a prompt. By default, the command below will create an interactive prompt and return relatively short outputs (256 tokens) with a sampling temperature of 0.5; this behavior can be changed in `/gpt-neox/checkpoints/configs/text_generation.yml`.

A lower temperature (e.g., 0.2) will produce more consistent and plausible (to the model) predictions; a higher temperature such as the default may be useful for generating and evaluating many candidates (see [our paper](https://arxiv.org/pdf/2202.13169) for recommendations). For the latter setting, consider switching to the `input-file` mode and providing an entire snippet (without escaping whitespace) in the corresponding file

## Multi-lingual Models<a name="models"></a>
Several models have been trained on a [large corpus](#data-characteristics) of code spanning 12 programming languages. This includes a 2.7B parameter model (nick-named **PolyCoder**, trained for 100K and 150K steps), a 405M parameter model (100K & 150K steps) and a 160M parameter model (150K steps).

### Available Models
All models are available [at a public Zenodo repository](https://zenodo.org/record/6363556), in the form of `.tar` files with fairly self-explanatory names (e.g., 2-7B-100K => a 2.7B parameter model trained for 100K steps). Currently available models include:

* **[GPT2 - 2.7B](https://zenodo.org/record/6363556/files/2-7B-150K.tar):** A 32 layer, 2,560 dimensional Transformer model, trained with a batch size of 128 sequences (256K tokens). Models available both at 100K and at 150K steps steps.
  * Note that GPT-Neox' [default config](https://github.com/EleutherAI/gpt-neox/blob/main/configs/2-7B.yml) for this model was modified to reduce the number of training steps (and learning rate decay steps accordingly) to 160K, down from 320K, to better match the available training resources. Hence, this model may not have reached its peak performance.
* **[GPT2 - 0.4B](https://zenodo.org/record/6363556/files/0-4B-150K.tar):** A 24 layer, 1,024 dimensional Transformer model based on the [`medium` config](https://github.com/EleutherAI/gpt-neox/blob/main/configs/medium.yml), trained with 256K tokens per batch.
* **[GPT2 - 160M](https://zenodo.org/record/6363556/files/160M-150K.tar):** A 12 layer, 768 dimensional Transformer model based on the [`small` config](https://github.com/EleutherAI/gpt-neox/blob/main/configs/small.yml), trained with 256K tokens per batch.

### Training Process
Training was done on 4 to 8 NVIDIA RTX 8000 GPUs, largely following the standard config values, except also enabling "scaled-upper-triang-masked-softmax-fusion" and "bias-gelu-fusion" for performance and slightly changing the batch size (see [model details](#available-models)), data split (changed to 98.9%, 0.1%, 1%), initial loss scale (2^16), and print/eval intervals.

The below image shows the loss curve of the various models' training process in terms of validation loss.
![image](https://user-images.githubusercontent.com/1426353/153651075-a0ceb8ef-6207-4853-b801-40dd6172d5a6.png)

### Caveats
The trained models come with a few minor known limitations:
- This model was not trained to solve programming problems and may not perform well on a benchmark such as [HumanEval](https://github.com/openai/human-eval). Models like Codex (powering Copilot) are pretrained on natural language, which may boost their ability to interpret NL prompts; this model only learned language from comments in code.
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


## Datasets

### 249GB Multi-Lingual Corpus
This is the corpus used to train PolyCoder. 

The datasets were cloned overnight on October 9-10, 2021. To mine a similar training set, see [Data](https://github.com/VHellendoorn/Code-LMs/tree/main/Data).

The list of file paths can be downloaded from: [https://zenodo.org/record/6363556/files/index.zip](https://zenodo.org/record/6363556/files/index.zip). 
Each row in the file is the file path along with its SHA-256 hash, to ease deduplication. That is, the hashes allow checking if files from any future test set were already contained in the training set.

The data collection and filtering process is described in detail in [the paper](https://arxiv.org/pdf/2202.13169.pdf) and below. The final, filtered dataset statistics are:

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

### Data Collection & Filtering
I cloned the most popular repositories for 12 popular programming languages with at least 50 stars (stopping at ~25K per langauge) from GitHub in October 2021. For each project, each file belonging to the majority-language of that project was extracted, yielding the training set below (after cleaning). This initial, unfiltered dataset spanned 631GB and 38.9M files.

Next, similar to Codex and CodeParrot, very large (>1MB) and very short (<100 tokens) files were filtered out, reducing the dataset to 424GB. Files were then deduplicated based on a hash of their content, which reduced the number of files by another 30% or so, leaving 249GB of data and 24.1M files. No tokenization filters were applied; the model processes entire files including all comments. A code-specific vocabulary was constructed on a random 5% subset of the files above.

## Evaluation
Please find detailed instructions for replicating our perplexity and HumanEval results on [our public fork](https://github.com/frankxu2004/gpt-neox#a-modified-version-for-polycoder-code-pretraining) of the NeoX repository. This in turn leverages [our extension](https://github.com/frankxu2004/lm-evaluation-harness) of the LM Evaluation Harness.

### Evaluating Codex
To download the test sets that we used in the paper (12 programming languages), use:
```
wget https://zenodo.org/record/6363556/files/unseen_test_sets.tar.gz
tar -xvzf unseen_test_sets.tar.gz
```

To get perplexity results on these samples using Codex' API, use:
```
export OPENAI_API_KEY=<YOUR OPEN AI API KEY>
python3 -u Evaluation/eval_codex_all.py --dirs Code-sampled100
```
Where `<YOUR OPEN AI API KEY>` is a private string that can be obtained by signing up for [OpenAI's beta](https://beta.openai.com/account/api-keys).

As of **March 2022**, getting an API Key is free for 3 months, and afterwards a credit card needs to be entered. However, even after entering a credit card, using our evaluation script does not lead to any costs.

### Results - HumanEval
These are PolyCoder's results on the [HumanEval benchmark](https://github.com/openai/human-eval):

|Model|Pass@1|Pass@10|Pass@100|
|------|-----|-----|-------|
|PolyCoder (160M) | 2.13% | 3.35% | 4.88% |
|PolyCoder (400M) | 2.96% | 5.29% | 11.59% |
|PolyCoder (2.7B) | 5.59% | 9.87% | 17.68% |
| CodeParrot (110M) | 3.80% | 6.57% | 12.78% |
| CodeParrot (1.5B) | 3.58% | 8.03% | 14.96% | 
| GPT-Neo (125M) | 0.75% | 1.88% | 2.97% | 
| GPT-Neo (1.3B) | 4.79% | 7.47% | 16.30% | 
| GPT-Neo (2.7B) | 6.41% | 11.27% | 21.37% | 
| GPT-J (6B) | 11.62% | 15.74% | 27.74% | 
| Codex (300M) | 13.17% | 20.37% | 36.27% | 
| Codex (2.5B) | 21.36% | 35.42% | 59.50% | 
| Codex (12B) | 28.81% | 46.81% | 72.31% | 


### Results - Multilingual Language Modeling
These are the perplexity results of PolyCoder on the [multilingual test sets](https://zenodo.org/record/6363556/files/unseen_test_sets.tar.gz):

|Language| Perplexity |
|------|-----|
|C | 2.3464 |
|C# | 2.5832 |
|C++ | 2.9189 |
|Go | 2.567 |
|Java | 2.9194 |
|JavaScript | 3.0611 |
|PHP | 3.6954 |
|Python | 3.1767 |
|Ruby | 3.9742 |
|Rust | 3.2449 |
|Scala | 3.8735 |
|TypeScript | 3.6143 |

A comparison with the other models is available in Figure 6 in the paper:
![image](images/fig6.png)

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
