# -*- coding: utf-8 -*-


import sys
import os
import torch
from collections import OrderedDict


input_dir = sys.argv[1]
output_file = "./0-4B/pytorch_model.bin"

layer_files = []
layer_id = -1
state_dict = OrderedDict()
for root, dirs, files in os.walk(input_dir):
    for file in files:
        if file.startswith("layer_"):
            # print(file)
            layer_files.append(os.path.join(root, file))


layer_files = sorted(layer_files)
for file in layer_files:
    print(file)
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
        print("Convert \"{}\" to \"{}\"".format(key, new_key))

torch.save(state_dict, output_file)
