import os
import torch
import argparse
from collections import OrderedDict
from transformers import GPTNeoXConfig, GPTNeoXForCausalLM, GPT2Tokenizer


def get_hf_state_dict_from_pt_files(checkpoint_dir):
    layer_files = []
    for root, dirs, files in os.walk(checkpoint_dir):
        for file in files:
            if file.startswith("layer_"):
                # print(file)
                layer_files.append(os.path.join(root, file))
    layer_files = sorted(layer_files)

    layer_id = -1
    state_dict = OrderedDict()
    for file in layer_files:
        print(f"Loading: {file}")
        new_layer = True

        module = torch.load(file, map_location=torch.device('cpu'))
        for key, value in module.items():
            if "word_embeddings" in key:
                new_key = key.replace("word_embeddings", "gpt_neox.embed_in")
                state_dict[new_key] = value
            elif "_layernorm" in key or "attention" in key or "mlp" in key:
                if new_layer:
                    layer_id += 1
                    new_layer = False
                new_key = "gpt_neox.layers." + str(layer_id) + "." + key
                state_dict[new_key] = value
            elif key.startswith("norm."):
                new_key = "gpt_neox.final_layer_norm." + key.split(".")[-1]
                state_dict[new_key] = value
            elif "final_linear" in key:
                new_key = "embed_out." + key.split(".")[-1]
                state_dict[new_key] = value
            print(f"Convert \"{key}\" to \"{new_key}\"")

    return state_dict


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--checkpoint_dir",
                        type=str,
                        required=True,
                        help="Directory that contains .pt files.")
    parser.add_argument("--vocab_file",
                        type=str,
                        required=True,
                        help="Path to the vocab file.")
    parser.add_argument('--merge_file',
                        type=str,
                        required=True,
                        help='Path to the BPE merge file.')
    parser.add_argument("--hf_config_path",
                        type=str,
                        required=True,
                        help="Path to HuggingFace configuration file.")
    parser.add_argument("--hf_save_dir",
                        type=str,
                        required=True,
                        help="Directory to save HuggingFace model.")
    args = parser.parse_args()

    config = GPTNeoXConfig.from_json_file(args.hf_config_path)

    model = GPTNeoXForCausalLM(config)
    state_dict = get_hf_state_dict_from_pt_files(args.checkpoint_dir)
    missing_keys, unexpected_keys = model.load_state_dict(state_dict, strict=False)
    print(f"missing keys: {missing_keys}")
    print(f"unexpected keys: {unexpected_keys}")

    tokenizer = GPT2Tokenizer(args.vocab_file, args.merge_file)

    if not os.path.exists(args.hf_save_dir):
        os.makedirs(args.hf_save_dir)
    print(f"Save HuggingFace model to {args.hf_save_dir} ...")
    model.save_pretrained(args.hf_save_dir)
    tokenizer.save_pretrained(args.hf_save_dir)
    print(f"Finished.")
