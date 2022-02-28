# coding:utf-8
"""
main code for clustering
define clustering class
and running this code to get clustering for stanford data
"""
import json
from source.AuxiliaryTools.nlp_tool import  low_case_tokenizer

all_file = ['dev.json', 'test.json', 'train.json']
all_task = ["navigate", "schedule", "weather"]


class Cluster:
    def __init__(self, input_dir, result_dir):
        print('This version of cluster is made for pseudo-labeled slot\intent data')
        self.input_dir = input_dir
        self.result_dir = result_dir

        # Tips:
        # you can't just assign {} as values when build dict with dict.fromkeys()
        # because {}' one 1 is viewed as 1 address, result in that all {} in fact point to same memory
        # "for" is only secure unless you use map:
        # all_data = dict(zip(all_task, map(lambda x:{},[None] * len(all_task))))
        self.all_data_item = dict.fromkeys(all_task)  # store all data item

        # Two dict for temp refill
        # 1. Template query dictionary
        # On the cluster directory, the key is the template text, the values are sub-dictionary,
        # whose key is the slot name in the template, the value is a list, and the content is appeared slot value.
        # 2. full dictionary
        # On the cluster directory, the key is slot name, the value is list, and the contents are all possible values
        self.all_temp_dict = dict.fromkeys(all_task)
        self.all_full_dict = dict.fromkeys(all_task)
        for key in all_task:
            # init store all data item
            self.all_data_item[key] = []
            # init template refill dict
            self.all_temp_dict[key] = {}
            # init full query dict
            self.all_full_dict[key] = {}

    def unpack_and_cook_raw_data(self, raw_data):
        # empty the all_data_item pool for current data
        for task in all_task:
            self.all_data_item[task] = []
        for dialogue in raw_data:
            task = dialogue['scenario']['task']['intent']
            uuid = dialogue['scenario']['uuid']
            turn_lst = dialogue['dialogue']
            # data extraction
            for ind, turn in enumerate(turn_lst):
                if turn['turn'] == "driver":
                    agent_reply = None
                    for remained_turn in turn_lst[ind:]:  # to get next agent reply
                        if remained_turn['turn'] == "assistant":
                            agent_reply = remained_turn
                            break
                    if agent_reply and agent_reply['data']['slots']:
                        common_slot = '_'.join(sorted(agent_reply['data']['slots'].keys()))
                        agent_say = agent_reply['data']['utterance']
                    else:  # cast to blank category
                        agent_say = None
                        common_slot = 'no_slot'
                    ori_pair = [turn, agent_reply]
                    user_say = turn['data']['utterance']

                    data_item = {
                        'ori_pair': ori_pair,
                        'user_say': user_say,
                        'user_temp': '',
                        'agent_say': agent_say,
                        'agent_temp': '',
                        'uuid': uuid
                    }
                    data_item = self.entity_replace(data_item)
                    self.all_data_item[task].append([common_slot, data_item])
                    # fill the dict for temp refilling
                    self.update_dict(task, data_item)

    def cluster_by_slot(self, target_file, split_rate_lst):
        print('Start %s clustering by slot' % target_file)
        data_label = target_file.replace('.json', '')
        raw_data = self.load_data(self.input_dir + target_file)
        self.unpack_and_cook_raw_data(raw_data)
        # cluster and output results
        for task in all_task:
            data_item_set_lst = []  # store different size of data item set
            if split_rate_lst and 'train' in data_label:
                for split_rate in split_rate_lst:
                    end_index = int(len(self.all_data_item[task]) * split_rate)
                    data_item_set_lst.append(self.all_data_item[task][:end_index])
            else:
                data_item_set_lst = [self.all_data_item[task]]
                split_rate_lst = [1.0]

            for ind, data_item_set in enumerate(data_item_set_lst):
                # clustering data here
                clustered_data = {}
                for common_slot, data_item in data_item_set:
                    if common_slot in clustered_data:
                        clustered_data[common_slot].append(data_item)
                    else:
                        clustered_data[common_slot] = [data_item]
                with open(self.result_dir + data_label + '_' + task + str(split_rate_lst[ind]) + '.json', 'w') as writer:
                    json.dump(clustered_data, writer, indent=2)

    def update_dict(self, task, data_item):
        agent_reply = data_item['ori_pair'][1]
        if agent_reply:
            user_temp = ' '.join(low_case_tokenizer(data_item['user_temp']))
            if user_temp not in self.all_temp_dict[task]:
                self.all_temp_dict[task][user_temp] = {}
            for (slot_name, slot_value) in agent_reply['data']['slots'].items():
                slot_value = slot_value.lower().strip()
                slot_name = slot_name.lower().strip()
                # update temp query dict
                if slot_name in self.all_temp_dict[task][user_temp]:
                    self.all_temp_dict[task][user_temp][slot_name].append(slot_value)
                else:
                    self.all_temp_dict[task][user_temp][slot_name] = [slot_value]
                # update full dict
                if slot_name in self.all_full_dict[task]:
                    self.all_full_dict[task][slot_name].append(slot_value)
                else:
                    self.all_full_dict[task][slot_name] = [slot_value]
        else:
            # print('no reply')
            pass
        return data_item

    def dump_dict(self):
        for task in all_task:
            with open(self.result_dir + task + '_temp-query.dict', 'w') as writer:
                json.dump(self.all_temp_dict[task], writer, indent=2)
            with open(self.result_dir + task + '_full-query.dict', 'w') as writer:
                json.dump(self.all_full_dict[task], writer, indent=2)

    @staticmethod
    def load_data(target_path):
        with open(target_path, 'r') as reader:
            json_data = json.load(reader)
        return json_data

    @staticmethod
    def entity_replace(data_item):
        agent_reply = data_item['ori_pair'][1]
        if agent_reply:
            data_item['user_temp'] = data_item['user_say'].lower()
            data_item['agent_temp'] = data_item['agent_say'].lower() if data_item['agent_say'] else ''
            for (slot_name, slot_value) in agent_reply['data']['slots'].items():
                slot_value = slot_value.lower().strip()
                slot_name = slot_name.lower().strip()
                data_item['user_temp'] = data_item['user_temp'].replace(slot_value, '<' + slot_name + '>')
                data_item['agent_temp'] = data_item['agent_temp'].replace(slot_value, slot_name)
        else:
            # print('no reply')
            pass
        return data_item


def slot_clustering_and_dump_dict(config=None, train_set_split_rate_lst=None):
    print('user utterance clustering')
    if not config:
        with open('../../config.json', 'r') as con_f:
            config = json.load(con_f)

    tmp_cluster = Cluster(
        input_dir=config['path']['RawData']['stanford'],
        result_dir=config['path']['ClusteringResult']
    )

    for f in all_file:
        tmp_cluster.cluster_by_slot(f, train_set_split_rate_lst)
        debug_n = 0
        for v in tmp_cluster.all_data_item.values():
            debug_n += len(v)
        print("debug!:", debug_n)

    tmp_cluster.dump_dict()
