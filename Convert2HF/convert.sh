size=${1:-0-4B}

python convert_neox_pt_to_huggingface_neox.py \
    --checkpoint_dir ../checkpoints/checkpoints-${size}/global_step150000 \
    --vocab_file ../Data/code-vocab.json \
    --merge_file ../Data/code-merges.txt \
    --hf_config_path ./polycoder/configs/config_${size}.json \
    --hf_save_dir ./polycoder/${size}
