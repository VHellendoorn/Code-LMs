# Copyright (c) 2015 Intel, Inc. All rights reserved.
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

from congress.api import api_utils
from congress.api import base
from congress.api import webservice
from congress import exception


class ActionsModel(base.APIModel):
    """Model for handling API requests about Actions."""

    # Note(dse2): blocking function
    def get_items(self, params, context=None):
        """Retrieve items from this model.

        :param: params: A dict-like object containing parameters
            from the request query string and body.
        :param: context: Key-values providing frame of reference of request

        :returns: A dict containing at least a 'actions' key whose value is a
            list of items in this model.
        """
        # Note: blocking call
        caller, source_id = api_utils.get_id_from_context(context)

        try:
            rpc_args = {'source_id': source_id}
            # Note(dse2): blocking call
            return self.invoke_rpc(caller, 'get_actions', rpc_args)
        except exception.CongressException as e:
            raise webservice.DataModelException(
                exception.NotFound.code, str(e),
                http_status_code=exception.NotFound.code)
