# Code Data Collection
This directory contains scripts to [mine](#mining) a dataset of code  similar to the one used to train [PolyCoder](https://arxiv.org/pdf/2202.13169.pdf), as well as details of that dataset. Note that because of the nature of the GH API, the exact results of each query will be different, so this will not precisely replicate the training data

## Mining
Update `gh_crawler.py` by adding your GH API token (line 6). Then, run `collect_data.sh`, which invokes the GitHub API crawler (`gh_crawler.py`), followed by a repo cloning script (`clone_repo.sh`, in parallel), which uses `extract_code.py` to extract all source code files in the corresponding language (and filter very long/short files), and finally `deduplicate.py` to remove duplicate files.

Once this is completed, you can use [gpt-neox](https://github.com/EleutherAI/gpt-neox)'s `preprocess_data.py` (currently in `tools/`) to tokenize this dataset for the model, using a either the pretrained code vocabularies by providing the `code-vocab.json` and `code-merges.txt` files, or producing a new one.

At the time of this writing*, the following command processes the entire `Code/` directory to a new directory named `Preprocessed/` using the pretrained vocabularies across 16 parallel workers (assuming that `gpt-neox` is checked out in the current directory):
```
mkdir Preprocessed
sudo python3 gpt-neox/tools/preprocess_data.py --input Code --tokenizer-type GPT2BPETokenizer --vocab-file code-vocab.json --merge-file code-merges.txt --output-prefix Preprocessed/code --workers 16
```
And that's it! Just modify the `local_setup.yml` config in the gpt-neox toolkit to point it to the new vocab & merges file and data directory and it should be able to train.

*I did have to modify the `yield_from_files` function to recursively yield all (shuffled) files from a directory; the default version uses `lm_dataformat`, which balks at code file extensions. The updated function can be found in `yield_from_code_files.py`.

## PolyCoder Data
The approach above was used to collect 249GB of multi-lingual training data to train [PolyCoder](https://arxiv.org/pdf/2202.13169.pdf) -- see the paper and top-level directory for details. Because of the ever-changing nature of repos on GitHub, running the above won't get you back the exact data, which is quite fine for most purposes (our training run didn't even use all of it), but it's naturally useful to know what data we used. We therefore release a list of all files used for training and their SHA-256 hash in [this file](https://zenodo.org/record/6341643/files/index.zip) (warning: zipped, still large), formatted as `{language}__{organization}__{project}__{full__file__path}\tSHA` (using double underscores instead of slashes in the file path).

To check whether a file was used during training, I strongly encourage considering not just its path but also its hashed contents. Files are often duplicated verbatim across and within projects. The following Python code was used to create the hash values in the file above, which allows fast deduplication of a new file against the set of all hashes used in our training data:

```python
import hashlib
with open(file_path, 'rb') as f:
	bytes = f.read()
	hash = hashlib.sha256(bytes).hexdigest();
```
