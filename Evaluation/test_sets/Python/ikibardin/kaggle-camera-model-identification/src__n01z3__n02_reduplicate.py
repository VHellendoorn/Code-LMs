# -*- coding: utf-8 -*-
"""Fast multithreaded image deduplicator
"""
import argparse
import glob
import multiprocessing
import os
import time
from collections import defaultdict

import cv2
import imagehash
from PIL import Image


def calc_hash(image):
    h = imagehash.phash(Image.fromarray(image), hash_size=16)
    return h


def process_image(image_path):
    try:
        image = cv2.imread(image_path)
        brief_hash = calc_hash(image)
    except:
        print('Invalid image: {}'.format(image_path))
        return None
    return image_path, brief_hash


parser = argparse.ArgumentParser(description='Deduplicator')
parser.add_argument('--num-threads', type=int, default=8)
parser.add_argument('--delete', type=bool, default=True)
args = parser.parse_args()

for dir in sorted(glob.glob('../../data/merge/*')):
    print(dir)
    image_list = glob.glob(os.path.join(dir, '**/*'), recursive=True)
    image_list = [os.path.abspath(p) for p in image_list]

    print('Number of images in dir "{}" = {}'.
          format(u'', len(image_list)))

    print('Start processing with {} threads'.format(args.num_threads))
    pool = multiprocessing.Pool(args.num_threads)
    ts = time.time()
    index = defaultdict(list)
    result = pool.imap(process_image, image_list, chunksize=500)

    for i, hashes in enumerate(result):
        if hashes is not None:
            index[hashes[1]].append(hashes[0])
        if i % 5000 == 0:
            print('processed {}/{}'.format(i, len(image_list)))

    pool.close()
    pool.join()

    index_build_time = time.time() - ts
    ts = time.time()

    dup_unique_cnt = 0
    dup_images_cnt = 0
    for image_list in index.values():
        if len(image_list) > 1:
            dup_list = [path for path in image_list]
            dup_images_cnt += len(dup_list)
            dup_unique_cnt += 1

            if args.delete:
                [os.remove(path) for path in dup_list[1:]]

    print('index build time: {}'.format(index_build_time))
    print('index search time: {}'.format(time.time() - ts))
    print('founded {} duplicated images in {} unique image groups'.
          format(dup_images_cnt, dup_unique_cnt))
