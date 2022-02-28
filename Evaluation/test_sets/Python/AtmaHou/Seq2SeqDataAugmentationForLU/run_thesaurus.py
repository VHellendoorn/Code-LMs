# coding: utf-8
import json
import argparse
from source.AuxiliaryTools.nlp_tool import low_case_tokenizer, sentence_edit_distance
from source.ReFilling.re_filling import re_filling
from set_config import refresh_config_file

# ============ Description ==========
# refill source file to test refill only

# ============ Args Process ==========
# General function
parser = argparse.ArgumentParser()
parser.add_argument("-t", "--task", type=str, default='navigate_labeled', help="choose task: atis_labeled, navigate_labeled, schedule_labeled, weather_labeled, navigate, schedule, weather")
parser.add_argument("-rf", "--refill", help="run surface realization", action="store_true")
parser.add_argument("-f", "--full", help="run all part", action="store_true")
parser.add_argument("-svt", "--slot_value_table", type=str, default='train', help='select use which slot value table: "fill", "train"')

# Deep Customize
parser.add_argument('--config', default='./config.json', help="specific a config file by path")
args = parser.parse_args()

# ============ Refresh Config ==========
refresh_config_file(args.config)

# ============ Settings ==========
TASK_NAME = args.task
RUN_REFILL = args.refill or args.full
with open(args.config, 'r') as con_f:
    CONFIG = json.load(con_f)


def refill_source_template(task, target_file, slot_value_table, split_rate):
    re_filling(CONFIG, task=task, target_file_name=target_file, split_rate=split_rate, slot_value_table=slot_value_table, refill_only=True)


if __name__ == '__main__':
    # for split_rate in [0.005]:
    #     for cluster_method in ['_intent-slot']:
    for split_rate in CONFIG['experiment']['train_set_split_rate']:
        for cluster_method in ['_intent-slot']:
        # for cluster_method in CONFIG['experiment']['cluster_method']:
            if RUN_REFILL:
                print('Start to refill for', 'train_' + TASK_NAME + cluster_method + str(split_rate) + '_src.txt')
                refill_source_template(TASK_NAME, 'train_' + TASK_NAME + cluster_method + str(split_rate) + '_src.txt', args.slot_value_table, split_rate)
