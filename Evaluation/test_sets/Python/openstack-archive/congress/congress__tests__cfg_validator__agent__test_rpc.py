#
# Copyright (c) 2017 Orange.
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

"""
Unit Tests for Congress agent RPC
"""

from congress.cfg_validator.agent import rpc
from congress.tests import base_rpc


class TestValidatorDriverApi(base_rpc.BaseTestRpcClient):
    """Unit tests for RPC on the driver"""

    def test_process_templates_hashes(self):
        "unit test for process_templates_hashes"
        rpcapi = rpc.ValidatorDriverClient()
        self._test_rpc_api(
            rpcapi,
            None,
            'process_templates_hashes',
            rpc_method='call',
            hashes='fake_hashes',
            host='host'
        )

    def test_process_configs_hashes(self):
        "unit test for process_configs_hashes"
        rpcapi = rpc.ValidatorDriverClient()
        self._test_rpc_api(
            rpcapi,
            None,
            'process_configs_hashes',
            rpc_method='call',
            hashes='fake_hashes',
            host='host'
        )
