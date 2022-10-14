from transformers import GPTNeoXForCausalLM, GPT2Tokenizer
import torch
import argparse


def load_model_and_generate(model_name_or_path, prompt, gen_kwargs):
    tokenizer = GPT2Tokenizer.from_pretrained(model_name_or_path)
    model = GPTNeoXForCausalLM.from_pretrained(model_name_or_path)

    encoded_input = tokenizer(prompt, return_tensors="pt")
    input_ids, attention_mask = encoded_input['input_ids'], encoded_input['attention_mask']
    if torch.cuda.is_available():
        model = model.cuda()
        input_ids = input_ids.cuda()
        attention_mask = attention_mask.cuda()

    prediction_ids = model.generate(input_ids=input_ids, attention_mask=attention_mask, **gen_kwargs)[0]
    prediction_tokens = tokenizer.decode(prediction_ids, skip_special_tokens=True, clean_up_tokenization_spaces=False)[len(prompt):]
    print(prompt + prediction_tokens)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()

    parser.add_argument("--model_name_or_path", type=str, default="./polycoder/0-4B")
    parser.add_argument('--temperature', type=float, default=0.2)
    parser.add_argument('--top_p', type=float, default=0.95)
    parser.add_argument('--max_length', type=int, default=128)

    args = parser.parse_args()

    gen_kwargs = {
        "do_sample": True,
        "temperature": args.temperature,
        "max_length": args.max_length,
        "top_p": args.top_p,
    }

    prompt = "\ndef add(x: int, y: int):\n    \"\"\"Add two numbers x and y\n    >>> add(2, 3)\n    5\n    >>> add(5, 7)\n    12\n    \"\"\"\n"

    load_model_and_generate(args.model_name_or_path, prompt, gen_kwargs)
