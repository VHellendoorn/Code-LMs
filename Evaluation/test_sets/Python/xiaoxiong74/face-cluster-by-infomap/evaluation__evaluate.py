#!/usr/bin/env python
# -*- coding: utf-8 -*-

import inspect
import argparse
import numpy as np

from evaluation import metrics
from utils import Timer, TextColors


def _read_meta(fn):
    labels = list()
    lb_set = set()
    with open(fn) as f:
        for lb in f.readlines():
            lb = int(lb.strip())
            labels.append(lb)
            lb_set.add(lb)
    return np.array(labels), lb_set


def evaluate(gt_labels, pred_labels, metric='pairwise'):
    if isinstance(gt_labels, str) and isinstance(pred_labels, str):
        print('[gt_labels] {}'.format(gt_labels))
        print('[pred_labels] {}'.format(pred_labels))
        gt_labels, gt_lb_set = _read_meta(gt_labels)
        pred_labels, pred_lb_set = _read_meta(pred_labels)

        print('#inst: gt({}) vs pred({})'.format(len(gt_labels),
                                                 len(pred_labels)))
        print('#cls: gt({}) vs pred({})'.format(len(gt_lb_set),
                                                len(pred_lb_set)))

    metric_func = metrics.__dict__[metric]

    with Timer('evaluate with {}{}{}'.format(TextColors.FATAL, metric,
                                             TextColors.ENDC)):
        result = metric_func(gt_labels, pred_labels)
    if isinstance(result, np.float):
        print('{}{}: {:.4f}{}'.format(TextColors.OKGREEN, metric, result,
                                      TextColors.ENDC))
    else:
        ave_pre, ave_rec, fscore = result
        print('{}ave_pre: {:.4f}, ave_rec: {:.4f}, fscore: {:.4f}{}'.format(
            TextColors.OKGREEN, ave_pre, ave_rec, fscore, TextColors.ENDC))


if __name__ == '__main__':
    metric_funcs = inspect.getmembers(metrics, inspect.isfunction)
    metric_names = [n for n, _ in metric_funcs]

    parser = argparse.ArgumentParser(description='Evaluate Cluster')
    parser.add_argument('--gt_labels', type=str, required=True)
    parser.add_argument('--pred_labels', type=str, required=True)
    parser.add_argument('--metric', default='pairwise', choices=metric_names)
    args = parser.parse_args()

    evaluate(args.gt_labels, args.pred_labels, args.metric)
