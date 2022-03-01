# Copyright (c) 2018 VMware Inc
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
# implied.
# See the License for the specific language governing permissions and
# limitations under the License.

from __future__ import print_function
from __future__ import division
from __future__ import absolute_import

from datetime import datetime
from datetime import timedelta
import mock
import time

from congress.datasources import vitrage_driver
from congress.tests import base


class TestVitrageDriver(base.TestCase):

    def setUp(self):
        super(TestVitrageDriver, self).setUp()
        self.vitrage = vitrage_driver.VitrageDriver('test-vitrage')

    @mock.patch.object(vitrage_driver.VitrageDriver, 'publish')
    def test_webhook_alarm_activate(self, mocked_publish):
        test_payload = {
            "notification": "vitrage.alarm.activate",
            "payload": {
                "vitrage_id": "2def31e9-6d9f-4c16-b007-893caa806cd4",
                "resource": {
                    "vitrage_id": "437f1f4c-ccce-40a4-ac62-1c2f1fd9f6ac",
                    "name": "app-1-server-1-jz6qvznkmnif",
                    "update_timestamp": "2018-01-22 10:00:34.327142+00:00",
                    "vitrage_category": "RESOURCE",
                    "vitrage_operational_state": "OK",
                    "vitrage_type": "nova.instance",
                    "project_id": "8f007e5ba0944e84baa6f2a4f2b5d03a",
                    "id": "9b7d93b9-94ec-41e1-9cec-f28d4f8d702c"},
                "update_timestamp": "2018-01-22T10:00:34Z",
                "vitrage_category": "ALARM",
                "state": "Active",
                "vitrage_type": "vitrage",
                "vitrage_operational_severity": "WARNING",
                "name": "Instance memory performance degraded"}}

        self.vitrage._webhook_handler(test_payload)

        # check receive_timestamp
        self.assertTrue((
            abs(datetime.utcnow() -
                datetime.strptime(next(iter(self.vitrage.state['alarms']))[6],
                                  vitrage_driver.TIMESTAMP_FORMAT))
            <= timedelta(seconds=1)))

        self.assertEqual(1, len(self.vitrage.state['alarms']))

        expected_rows = set([(u'Instance memory performance degraded',
                              u'Active',
                              u'vitrage',
                              u'WARNING',
                              u'2def31e9-6d9f-4c16-b007-893caa806cd4',
                              u'2018-01-22T10:00:34Z',
                              next(iter(self.vitrage.state['alarms']))[6],
                              u'app-1-server-1-jz6qvznkmnif',
                              u'9b7d93b9-94ec-41e1-9cec-f28d4f8d702c',
                              u'437f1f4c-ccce-40a4-ac62-1c2f1fd9f6ac',
                              u'8f007e5ba0944e84baa6f2a4f2b5d03a',
                              u'OK',
                              u'nova.instance')])
        self.assertEqual(self.vitrage.state['alarms'], expected_rows)

    @mock.patch.object(vitrage_driver.VitrageDriver, 'publish')
    def test_webhook_alarm_deactivate(self, mocked_publish):
        test_payload = {
            "notification": "vitrage.alarm.deactivate",
            "payload": {
                "vitrage_id": "2def31e9-6d9f-4c16-b007-893caa806cd4",
                "resource": {
                    "vitrage_id": "437f1f4c-ccce-40a4-ac62-1c2f1fd9f6ac",
                    "name": "app-1-server-1-jz6qvznkmnif",
                    "update_timestamp": "2018-01-22 11:00:34.327142+00:00",
                    "vitrage_category": "RESOURCE",
                    "vitrage_operational_state": "OK",
                    "vitrage_type": "nova.instance",
                    "project_id": "8f007e5ba0944e84baa6f2a4f2b5d03a",
                    "id": "9b7d93b9-94ec-41e1-9cec-f28d4f8d702c"},
                "update_timestamp": "2018-01-22T11:00:34Z",
                "vitrage_category": "ALARM",
                "state": "Inactive",
                "vitrage_type": "vitrage",
                "vitrage_operational_severity": "OK",
                "name": "Instance memory performance degraded"}}

        self.vitrage.state['alarms'] = set([(
            u'Instance memory performance degraded',
            u'Active',
            u'vitrage',
            u'WARNING',
            u'2def31e9-6d9f-4c16-b007-893caa806cd4',
            u'2018-01-22T10:00:34Z',
            u'2018-01-22T10:00:34Z',
            u'app-1-server-1-jz6qvznkmnif',
            u'9b7d93b9-94ec-41e1-9cec-f28d4f8d702c',
            u'437f1f4c-ccce-40a4-ac62-1c2f1fd9f6ac',
            u'8f007e5ba0944e84baa6f2a4f2b5d03a',
            u'OK',
            u'nova.instance')])
        self.vitrage._webhook_handler(test_payload)

        self.assertEqual(1, len(self.vitrage.state['alarms']))

        expected_rows = set([(u'Instance memory performance degraded',
                              u'Inactive',
                              u'vitrage',
                              u'OK',
                              u'2def31e9-6d9f-4c16-b007-893caa806cd4',
                              u'2018-01-22T11:00:34Z',
                              next(iter(self.vitrage.state['alarms']))[6],
                              u'app-1-server-1-jz6qvznkmnif',
                              u'9b7d93b9-94ec-41e1-9cec-f28d4f8d702c',
                              u'437f1f4c-ccce-40a4-ac62-1c2f1fd9f6ac',
                              u'8f007e5ba0944e84baa6f2a4f2b5d03a',
                              u'OK',
                              u'nova.instance')])
        self.assertEqual(self.vitrage.state['alarms'], expected_rows)

    @mock.patch.object(vitrage_driver.VitrageDriver, 'publish')
    def test_webhook_alarm_cleanup(self, mocked_publish):
        self.vitrage = vitrage_driver.VitrageDriver(
            'test-vitrage',
            args={'hours_to_keep_alarm': 1 / 3600})  # set to 1 sec for test

        test_payload = {
            "notification": "vitrage.alarm.activate",
            "payload": {
                "vitrage_id": "2def31e9-6d9f-4c16-b007-893caa806cd4",
                "resource": {
                    "vitrage_id": "437f1f4c-ccce-40a4-ac62-1c2f1fd9f6ac",
                    "name": "app-1-server-1-jz6qvznkmnif",
                    "update_timestamp": "2018-01-22 10:00:34.327142+00:00",
                    "vitrage_category": "RESOURCE",
                    "vitrage_operational_state": "OK",
                    "vitrage_type": "nova.instance",
                    "project_id": "8f007e5ba0944e84baa6f2a4f2b5d03a",
                    "id": "9b7d93b9-94ec-41e1-9cec-f28d4f8d702c"},
                "update_timestamp": "2018-01-22T10:00:34Z",
                "vitrage_category": "ALARM",
                "state": "Active",
                "vitrage_type": "vitrage",
                "vitrage_operational_severity": "WARNING",
                "name": "Instance memory performance degraded"}}

        self.vitrage._webhook_handler(test_payload)

        self.assertEqual(1, len(self.vitrage.state['alarms']))
        time.sleep(3)
        self.assertEqual(0, len(self.vitrage.state['alarms']))
