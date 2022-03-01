# Copyright (c) 2017 VMware, Inc. All rights reserved.
#
#    Licensed under the Apache License, Version 2.0 (the "License"); you may
#    not use this file except in compliance with the License. You may obtain
#    a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
#    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
#    License for the specific language governing permissions and limitations
#    under the License.
#

from __future__ import print_function
from __future__ import division
from __future__ import absolute_import

import copy
import json
import jsonschema
import os

from oslo_config import cfg
from oslo_db import exception as db_exc
from oslo_log import log as logging
import yaml

from congress.datalog import compile
from congress.db import db_library_policies
from congress.dse2 import data_service
from congress import exception

LOG = logging.getLogger(__name__)


def validate_policy_item(item):
    schema_json = '''
    {
      "id": "PolicyProperties",
      "title": "Policy Properties",
      "type": "object",
      "required": ["name", "rules"],
      "properties": {
        "name": {
          "title": "Policy unique name",
          "type": "string",
          "minLength": 1,
          "maxLength": 255
        },
        "description": {
          "title": "Policy description",
          "type": "string"
        },
        "kind": {
          "title": "Policy kind",
          "type": "string",
          "enum": ["database", "nonrecursive", "action", "z3", "materialized",
                   "delta", "datasource"]
        },
        "abbreviation": {
          "title": "Policy name abbreviation",
          "type": "string",
          "minLength": 1,
          "maxLength": 5
        },
        "rules": {
          "title": "collection of rules",
          "type": "array",
          "items": {
            "title": "Policy rule",
            "type": "object",
            "required": ["rule"],
            "properties": {
              "rule": {
                "title": "Rule definition following policy grammar",
                "type": "string"
              },
              "name": {
                "title": "User-friendly name",
                "type": "string",
                "maxLength": 255
              },
              "comment": {
                "title": "User-friendly comment",
                "type": "string",
                "maxLength": 255
              }
            }
          }
        }
      }
    }
    '''
    try:
        jsonschema.validate(item, json.loads(schema_json))
    except jsonschema.exceptions.ValidationError as ve:
        raise exception.InvalidPolicyInput(data=str(ve))


class LibraryService (data_service.DataService):
    def __init__(self, name):
        data_service.DataService.__init__(self, name)
        self.name = name
        self.add_rpc_endpoint(DseLibraryServiceEndpoints(self))

    def create_policy(self, policy_dict):
        policy_dict = copy.deepcopy(policy_dict)
        validate_policy_item(policy_dict)
        policy_name = policy_dict['name']

        # check name is valid
        if not compile.string_is_servicename(policy_name):
            raise exception.PolicyException(
                'name `%s` is not a valid policy name' % policy_name)

        # make defaults
        if 'kind' not in policy_dict:
            policy_dict['kind'] = 'nonrecursive'
        if 'abbreviation' not in policy_dict:
            policy_dict['abbreviation'] = policy_name[:5]
        if 'description' not in policy_dict:
            policy_dict['description'] = ''

        try:
            # Note(thread-safety): blocking call
            policy = db_library_policies.add_policy(policy_dict=policy_dict)
            return policy.to_dict()
        except db_exc.DBError:
            LOG.exception('Creating a new library policy failed due to '
                          'backend database error.')
            raise

    def get_policies(self, include_rules=True):
        return [p.to_dict(include_rules)
                for p in db_library_policies.get_policies()]

    def get_policy(self, id_, include_rules=True):
        # Note(thread-safety): blocking call
        policy = db_library_policies.get_policy(id_)
        return policy.to_dict(include_rules)

    def get_policy_by_name(self, name, include_rules=True):
        # Note(thread-safety): blocking call
        policy = db_library_policies.get_policy_by_name(name)
        return policy.to_dict(include_rules)

    def delete_all_policies(self):
        # Note(thread-safety): blocking call
        db_library_policies.delete_policies()

    def delete_policy(self, id_):
        # Note(thread-safety): blocking call
        db_object = db_library_policies.get_policy(id_)
        db_library_policies.delete_policy(id_)
        return db_object.to_dict(include_rules=True)

    def replace_policy(self, id_, policy_dict):
        validate_policy_item(policy_dict)
        policy_name = policy_dict['name']

        # check name is valid
        if not compile.string_is_servicename(policy_name):
            raise exception.PolicyException(
                "Policy name %s is not a valid service name" % policy_name)

        # make defaults
        if 'kind' not in policy_dict:
            policy_dict['kind'] = 'nonrecursive'
        if 'abbreviation' not in policy_dict:
            policy_dict['abbreviation'] = policy_name[:5]
        if 'description' not in policy_dict:
            policy_dict['description'] = ''

        # Note(thread-safety): blocking call
        policy = db_library_policies.replace_policy(
            id_, policy_dict=policy_dict)
        return policy.to_dict()

    def load_policies_from_files(self):
        '''load library policies from library directory

        return total number of files on which error encountered
        '''
        def _load_library_policy_file(full_path):
            try:
                success_policy_count = 0
                error_policy_count = 0
                doc_num_in_file = 0
                file_error = False
                with open(full_path, "r") as stream:
                    policies = yaml.safe_load_all(stream)
                    for policy in policies:
                        try:
                            doc_num_in_file += 1
                            self.create_policy(policy)
                            success_policy_count += 1
                        except db_exc.DBDuplicateEntry:
                            LOG.debug(
                                'Library policy %s (number %s in file %s) '
                                'already exists (likely loaded by another '
                                'Congress instance). Skipping.',
                                policy.get('name', '[no name]'),
                                doc_num_in_file, full_path)
                        except exception.CongressException:
                            LOG.exception(
                                'Library policy %s could not be loaded. '
                                'Skipped. YAML reproduced here %s',
                                policy.get('name', '[no name]'),
                                yaml.dump(policy))
                            error_policy_count += 1
            except Exception:
                LOG.exception(
                    'Failed to load library policy file %s', full_path)
                file_error = True
            return success_policy_count, file_error or (error_policy_count > 0)
        file_count = 0
        file_error_count = 0
        policy_count = 0
        for (dirpath, dirnames, filenames) in os.walk(
                cfg.CONF.policy_library_path):
            for filename in filenames:
                name, extension = os.path.splitext(filename)
                if extension in ['.yaml', '.yml']:
                    count, has_error = _load_library_policy_file(
                        os.path.join(dirpath, filename))
                    if count > 0:
                        file_count += 1
                        policy_count += count
                    if has_error:
                        file_error_count += 1
        LOG.debug(
            '%s library policies from %s files successfully loaded; '
            'error encountered on %s files.',
            policy_count, file_count, file_error_count)
        return file_error_count


class DseLibraryServiceEndpoints(object):
    """RPC endpoints exposed by LibraryService."""

    def __init__(self, data_service):
        self.data_service = data_service

    def create_policy(self, context, policy_dict):
        return self.data_service.create_policy(policy_dict)

    def get_policies(self, context, include_rules=True):
        return self.data_service.get_policies(include_rules)

    def get_policy(self, context, id_, include_rules=True):
        return self.data_service.get_policy(id_, include_rules)

    def get_policy_by_name(self, context, name, include_rules=True):
        return self.data_service.get_policy_by_name(name, include_rules)

    def delete_all_policies(self, context):
        return self.data_service.delete_all_policies()

    def delete_policy(self, context, id_):
        return self.data_service.delete_policy(id_)

    def replace_policy(self, context, id_, policy_dict):
        return self.data_service.replace_policy(id_, policy_dict)
