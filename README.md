# Large Models of Source Code
I occasionally train and publicly release large neural language models on programs. Here, I describe how to use these.

## 2.7B Model of Assorted Languages
This is a 32 layer, 2,560 dimensional Transformer model on a [large corpus](#data-characteristics) of code from across 11 programming languages, trained for 100K steps using the [GPT-neox](https://github.com/EleutherAI/gpt-neox) toolkit.

### Availability & Usage
**Via DockerHub (recommended):** A public image containing a slightly modified version of the [gpt-neox repository](https://github.com/EleutherAI/gpt-neox) containing the final model checkpoint has been [pushed to DockerHub](https://hub.docker.com/repository/docker/vhellendoorn/neox-2-7b-code). The image size is 15GB and the model will require 6GB of GPU memory to run (running on CPU is neither tested nor recommended). Acquire and run it with the following commands (substituting another GPU device index if needed):
```
nvidia-docker run --rm -it -e NVIDIA_VISIBLE_DEVICES=0 --shm-size=1g --ulimit memlock=-1 vhellendoorn/neox-2-7b-code:trained100K
cd /gpt-neox
sudo ./deepy.py text_gen_gpt2.py -d checkpoints/configs 2-7B.yml text_generation.yml local_setup.yml
```

Once up, you can feed it an example such as `def return1():\n  """Returns 1."""\n  ` (note the whitespace) and watch it predict `return 1` (and then probably a bunch of other `returnX` methods, depending on the sample).

The modifications to gpt-neox mentioned above center around the need to allow tabs and newlines in the prompt input. For the _interactive_ mode, these can be added using their escaped versions (`\t`, `\n`); when using file-based input, the project will read the entire file instead of treating each line as a prompt. By default, the command below will create an interactive prompt and return relatively short outputs (128 tokens) with a sampling temperature of 0.8; this behavior can be changed in `/gpt-neox/checkpoints/configs/text_generation.yml`.

A lower temperature (e.g., 0.2) will produce more consistent and plausible (to the model) predictions; a higher temperature such as the default may be useful for generating and evaluating many candidates (see the [Codex paper](https://arxiv.org/pdf/2107.03374) for recommendations). For the latter setting, consider switching to the `input-file` mode and providing an entire snippet (without escaping whitespace) in the corresponding file

For those preferring to use just the checkpointed model, please find it (plus vocabulary files and configs) on this public [Google Drive](https://drive.google.com/file/d/1qLsQMBDyIZ2CZgr1unsteGHWGQz8J2fi/view?usp=sharing). Note the point above, about the need for allowing tabs and newlines when using this model.

### Data Characteristics
I cloned the most popular repositories for 11 popular programming languages with at least 50 stars (stopping at ~25K per langauge) from GitHub in October 2021. For each project, each file belonging to the majority-language of that project was extracted, yielding the training set below.

|Language|Repositories|Size(GB)|Files|
|------|-----|-----|-------|
|C | 10749 | 52G | 3,037,112 |
|C# | 9511 | 4.1G | 2,514,494 |
|C++ | 13726 | 22G | 4,289,506 |
|Go | 12371 | 3.5G | 1,416,789 |
|Java | 15044 | 55G | 5,120,129 |
|JavaScript | 25144 | 41G | 1,774,174 |
|PHP | 9960 | 9.2G | 1,714,058 |
|Python | 25446 | 21G | 1,550,208 |
|Ruby | 5826 | 13G | 674,343 |
|Rust | 4991 | 15G | 304,842 |
|Scala | 1497 | 1.8G | 245,100 |
|TypeScript | 12830 | 16G | 1,441,926 |

### Training Process
Very large (>1MB) and very short (<100 tokens) files were filtered out. Files were deduplicated based on a hash of their content, which reduced the dataset by nearly 30%. Otherwise, no tokenization filters were applied; the model processes entire files including all comments. A code-specific vocabulary was constructed on a random 5% subset of the files above. The model was then trained for 100K steps, each of which consisted of a batch 128 samples with 2,048 tokens each.

Training took ca. 3 weeks on 8 NVIDIA RTX 8000 GPUs, largely following the standard [2-7B.yml](https://github.com/EleutherAI/gpt-neox/blob/main/configs/2-7B.yml) values except also enabling "scaled-upper-triang-masked-softmax-fusion" and "bias-gelu-fusion" for performance and slightly changing the batch size, data split, initial loss scale, and print/eval intervals.

The below image shows the loss curve of the training process on validation data.
![image](https://user-images.githubusercontent.com/1426353/143721005-d535bc60-f04c-4592-a0e9-45738c2180d2.png)

### Caveats
The trained model has a few minor known limitations:
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
