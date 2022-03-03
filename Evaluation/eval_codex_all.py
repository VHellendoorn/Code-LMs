import argparse
import glob
import json
import os
import time
import math
import openai
import shutil
import pathlib

languages_to_run = {'C', 'C#', 'C++', 'Go', 'Java', 'JavaScript', 
                    'PHP', 'Python', 'Ruby', 'Rust', 'Scala', 'TypeScript'}

# The private OpenAI API key needs to be an environment variable
openai.api_key = os.getenv('OPENAI_API_KEY')
# As instructed here: https://community.openai.com/t/token-logprobs-when-echo-is-true/9626/2
# "Transformer models don’t predict the probability of the first token. If you want to get the probability 
# for your first token you can try to use <|endoftext|> as the first token as a workaround."
endoftext_token = '<|endoftext|>'

def ppl(avg_logprob):
    return 2 ** (-avg_logprob / math.log(2))

def call_codex(code_str, save_probs):
    eos_code_str = endoftext_token + code_str
    # engine: 'davinci-codex' is currently the best codex model
    # max_tokens=0 means that we don't want the model to generate additional tokens
    # logprobs=0 means that we don't want the logprobs of the alternative tokens, only the actual tokens
    # echo=True means that we want the model to echo our prompt, in addition to our (not existing) completion
    completion = openai.Completion.create(engine="davinci-codex", prompt=eos_code_str,
                                          max_tokens=0,
                                          temperature=0.0,
                                          logprobs=0,
                                          n=1,
                                          echo=True)
    
    c = completion.choices[0]
    # skipping the <|endoftext|> token
    sum_logprobs = sum(c.logprobs.token_logprobs[1:])
    num_tokens = len(c.logprobs.token_logprobs[1:])
    if save_probs:
        saved_probs = {
            'text': code_str,
            'tokens': c.logprobs.tokens[1:],
            'logprobs': c.logprobs.token_logprobs[1:],
            'sum_logprobs': sum_logprobs
        }
    else:
        saved_probs = None    

    return sum_logprobs, num_tokens, saved_probs

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--dirs', type=str, help='path to a directory that contains a subdirectory for each evaluated language', required=False)
    parser.add_argument('--save-probs', type=str, required=False, default=None)
    parser.add_argument('--output', type=str, required=False, default=os.devnull)
    args = parser.parse_args()
    
    results = {}
    dirs = glob.glob(os.path.join(args.dirs, '*'), recursive=False)
    excluded_dirs = args.dirs + '-excluded'
    pathlib.Path(excluded_dirs).mkdir(parents=True, exist_ok=True)
    for language in dirs:
        if language.split('/')[-1] not in languages_to_run:
            continue
        print('Language:', language)
        files = glob.glob(os.path.join(language, '**/*'), recursive=True)
        files = [f for f in files if os.path.isfile(f)]
            
        log_probs_sum = 0
        tokens_count = 0
        ignored_files = []
        all_per_token_probs = []
        with open(args.output, 'w') as out_file:
            for file in files:
                try:
                    with open(file, 'r') as f:
                        code_str = f.read()
                    logprobs_sum, logprobs_count, per_token_probs = call_codex(code_str, args.save_probs is not None)
                except Exception as e:
                    print(f'EXCEPTION in file {file}: {e}')
                    print(e)
                    ignored_files.append(file)
                    # OpenAI limits the request rate to 20/min
                    time.sleep(10)
                    continue
                out_str = f'{logprobs_sum}\t{logprobs_count}\t{file}'
                if args.output != os.devnull:
                    out_file.writelines([f'Evaluating file: {file}', out_str, '\n'])

                log_probs_sum += logprobs_sum
                tokens_count += logprobs_count
                # OpenAI limits the request rate to 20/min
                time.sleep(10)
        
        print(f'\n\n\nlogprobs sum: {log_probs_sum}')
        print(f'total tokens: {tokens_count}')
        print(f'Average loss: {-log_probs_sum / tokens_count}')
        print(f'Perplexity: {ppl(log_probs_sum / tokens_count)}')
        print(f'Ignored files:')
        for f in ignored_files:
            print(f'\t{f}')
            new_location = os.path.join(excluded_dirs, os.path.dirname(f))
            pathlib.Path(new_location).mkdir(parents=True, exist_ok=True)
            shutil.move(f, new_location)
        results[language] = {
            'log_probs_sum': log_probs_sum,
            'tokens_count': tokens_count,
            'average_loss': -log_probs_sum / tokens_count,
            'perplexity': ppl(log_probs_sum / tokens_count),
        }

    print('Language, sum_logprobs, average_loss, perplexity, num_tokens')
    for language in results:
        print(f'{language.split("/")[-1]}, {results[language]["log_probs_sum"]}, {results[language]["average_loss"]}, {results[language]["perplexity"]}, {results[language]["tokens_count"]}')
