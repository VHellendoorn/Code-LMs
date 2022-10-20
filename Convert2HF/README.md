# Convert to HuggingFace
This directory contains a script `convert_neox_pt_to_huggingface_neox.py` to convert PolyCoder checkpoints trained by [gpt-neox](https://github.com/EleutherAI/gpt-neox) into HuggingFace format, and a script `generate.py` to load the converted model and generate code from a given prompt.
Shoutout to @NinedayWang for implementing this!

## Environment
transformers 4.23.1

## Convert
You can use the `convert.sh` script to convert specified model to the HuggingFace format, using `./convert.sh 0-4B` (or pass a different model size). This script in turn invokes `convert_neox_pt_to_huggingface_neox.py`, which you can also call directly as follows:
```
python convert_neox_pt_to_huggingface_neox.py \
    --checkpoint_dir ../checkpoints/checkpoints-0-4B/global_step150000 \
    --vocab_file ../Data/code-vocab.json \
    --merge_file ../Data/code-merges.txt \
    --hf_config_path ./polycoder/configs/config_0-4B.json \
    --hf_save_dir ./polycoder/0-4B 
```
HuggingFace configuration files for different size models are provided in `polycoder/configs/`, including `config_0-4B.json`, `config_2-7B.json` and `config_160M.json`.

After running, you can get a complete HuggingFace model in the directory specified by `hf_save_dir`. If the directory does not exist, it can be built automatically.

## Generate
The following is an example to load the converted 0.4B HuggingFace model and generate code from a given prompt:
```
python generate.py \
    --model_name_or_path ./polycoder/0-4B \
    --temperature 0.2 \
    --top_p 0.95 \
    --max_length 128
```
You can evaluate models of other sizes by specifying `model_name_or_path`.
