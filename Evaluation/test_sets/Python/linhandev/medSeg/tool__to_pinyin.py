import os
import shutil

import argparse
from tqdm import tqdm

import util


parser = argparse.ArgumentParser("zip_dataset")
parser.add_argument("--in_dir", type=str)
parser.add_argument("--out_dir", type=str)
args = parser.parse_args()


def to_pinyin(name, nonum=False):
    new_name = ""
    for ch in name:
        if u"\u4e00" <= ch <= u"\u9fff":
            new_name += pinyin(ch, style=Style.NORMAL)[0][0]
        else:
            # if nonum and ("0" <= ch <= "9" or ch == "_"):
            #     continue
            new_name += ch
    return new_name


def main():
    files = os.listdir(args.in_dir)
    for f in tqdm(files):
        shutil.copy(
            os.path.join(args.in_dir, f),
            os.path.join(args.out_dir, to_pinyin(f)),
        )
        # input("here")


if __name__ == "__main__":
    main()
