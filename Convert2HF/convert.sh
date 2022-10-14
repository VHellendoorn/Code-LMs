python convert_neox_pt_to_huggingface_neox.py \
    --checkpoint_dir ../checkpoints/checkpoints-0-4B/global_step150000 \
    --vocab_file ../Data/code-vocab.json \
    --merge_file ../Data/code-merges.txt \
    --hf_config_path ./polycoder/configs/config_0-4B.json \
    --hf_save_dir ./polycoder/0-4B
