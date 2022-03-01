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
Utilities for testing RPC clients.
"""
import mock

from oslotest import base


# TODO(pc): all this does not ensure that transport and target make any sense.
# But not tested in neutron ml2 use case either.
class BaseTestRpcClient(base.BaseTestCase):
    """Abstract class providing functions to test client RPC"""

    def _test_rpc_api(self, rpcapi, topic, method, rpc_method, **kwargs):
        """Base function to test each call"""
        ctxt = {}
        expected_retval = 'foo' if rpc_method == 'call' else None
        expected_version = kwargs.pop('version', None)
        fanout = kwargs.pop('fanout', False)
        server = kwargs.get('server', None)

        with mock.patch.object(rpcapi.client, rpc_method) as rpc_mock,\
                mock.patch.object(rpcapi.client, 'prepare') as prepare_mock:
            prepare_mock.return_value = rpcapi.client
            rpc_mock.return_value = expected_retval
            retval = getattr(rpcapi, method)(ctxt, **kwargs)

        prepare_args = {}
        if expected_version:
            prepare_args['version'] = expected_version
        if fanout:
            prepare_args['fanout'] = fanout
        if topic:
            prepare_args['topic'] = topic
        if server:
            prepare_args['server'] = server
        prepare_mock.assert_called_once_with(**prepare_args)

        self.assertEqual(retval, expected_retval)
        if server:
            kwargs.pop('server')
        rpc_mock.assert_called_once_with(ctxt, method, **kwargs)
