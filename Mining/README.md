## Purpose
Scripts to construct a dataset of code in a similar way to the ones used to train the released models. Note that because of the nature of the GH API, the exact results of each query will be different, so this will not precisely replicate the training data.

## Usage
Update `gh_crawler.py` by adding your GH API token (line 6). Then, run `collect_data.sh`, which invokes the GitHub API crawler (`gh_crawler.py`), followed by a repo cloning script (`clone_repo.sh`, in parallel), which uses `extract_code.py` to extract all source code files in the corresponding language (and filter very long/short files), and finally `deduplicate.py` to remove duplicate files.

Once this is completed, you can use [gpt-neox](https://github.com/EleutherAI/gpt-neox)'s `preprocess_data.py` (currently in `tools/`) to tokenize this dataset for the model, using a either the pretrained code vocabularies by providing the `code-vocab.json` and `code-merges.txt` files, or producing a new one.

At the time of this writing*, the following command processes the entire `Code/` directory to a new directory named `Preprocessed/` using the pretrained vocabularies across 16 parallel workers (assuming that `gpt-neox` is checked out in the current directory):
```
mkdir Preprocessed
sudo python3 gpt-neox/tools/preprocess_data.py --input Code --tokenizer-type GPT2BPETokenizer --vocab-file code-vocab.json --merge-file code-merges.txt --output-prefix Preprocessed/code --workers 16
```
And that's it! Just modify the `local_setup.yml` config in the gpt-neox toolkit to point it to the new vocab & merges file and data directory and it should be able to train.

*I did have to modify the `yield_from_files` function to recursively yield all (shuffled) files from a directory; the default version uses `lm_dataformat`, which balks at code file extensions. The updated function can be found in `yield_from_code_files.py`.
