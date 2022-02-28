#!/usr/bin/python
###############################################################################
#  Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.    #
#                                                                             #
#  Licensed under the Apache License Version 2.0 (the "License"). You may not #
#  use this file except in compliance with the License. A copy of the License #
#  is located at                                                              #
#                                                                             #
#      http://www.apache.org/licenses/LICENSE-2.0/                                        #
#                                                                             #
#  or in the "license" file accompanying this file. This file is distributed  #
#  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express #
#  or implied. See the License for the specific language governing permis-    #
#  sions and limitations under the License.                                   #
###############################################################################

# Environmental Variables:
# SOLUTION_LOGGROUP: CW Logs group to log to. Default SO0111-SHARR

# *******************************************************************
# Required Modules:
# *******************************************************************
import os
import time
from datetime import date
import boto3
from botocore.exceptions import ClientError
from botocore.config import Config
from utils import partition_from_region
import awsapi_cached_client

LOG_MAX_BATCH_SIZE = 1048576    # Controls when the buffer is flushed to the stream
LOG_ENTRY_ADDITIONAL = 26

def get_logs_connection(apiclient):
    # returns a client id for ssm in the region of the finding via apiclient
    return apiclient.get_connection('logs')

class FailedToCreateLogGroup(Exception):
    pass

class LogHandler(object):

    def __init__(self, stream_name):

        self.apiclient = awsapi_cached_client.AWSCachedClient(os.getenv('AWS_DEFAULT_REGION', 'us-east-1'))
        self.stream_name = stream_name.upper()
        self.log_group = os.getenv('SOLUTION_LOGGROUP', 'SO0111-SHARR')
        self._stream_token = None
        self._buffer = []
        self._buffer_size = 0

    @property
    def streams_used(self):
        return self._stream_token

    def _create_log_group(self):
        """Create the application log group"""
        try:
            get_logs_connection(self.apiclient).create_log_group(
                logGroupName=self.log_group
                )
        except Exception as e:
            # if the stream was created in between the call ignore the error
            if type(e).__name__ != "ResourceAlreadyExistsException":
                return False
        return True

    def _create_log_stream(self, log_stream):
        """Create a new log stream"""
        # append today's date to stream name
        log_stream = log_stream + '-' + str(date.today())
        try:
            print(("Creating log stream {}".format(log_stream)))
            get_logs_connection(self.apiclient).create_log_stream(logGroupName=self.log_group, logStreamName=log_stream)
            self._stream_token = "0"
        except Exception as e:
            # if the stream was created in between the call ignore the error
            if type(e).__name__ == "ResourceAlreadyExistsException":
                print('Log Stream already exists')
            elif type(e).__name__ == "ResourceNotFoundException":
                if self._create_log_group():
                    get_logs_connection(self.apiclient).create_log_stream(logGroupName=self.log_group, logStreamName=log_stream)
                else:
                    raise FailedToCreateLogGroup
            else:
                raise e
        return log_stream

    def add_message(self, message):
        """Write a message to the buffer"""
        # Empty messages cause flush throw an exception
        if not message:
            message = '   '
        timestamp = int(time.time() * 1000)
        if self._buffer_size + (len(message) + LOG_ENTRY_ADDITIONAL) > LOG_MAX_BATCH_SIZE:
            self.flush()

        # put the timestamped message in the buffer
        self._buffer.append((timestamp, message))     
        self._buffer_size += (len(message) + LOG_ENTRY_ADDITIONAL) # calculate new buffer size

    def flush(self):
        """Write the buffer to the CW Logs stream"""
        # _create_log_stream will create the dated stream if it does not exist.
        # It returns the name of the current stream. This way we always write to a 
        # date-stamped stream. Ex CIS_1-3-2020-06-02 for CIS_1-3

        if self._buffer_size == 0:
            return
            
        log_stream = self._create_log_stream(log_stream=self.stream_name)

        put_event_args = {
            "logGroupName": self.log_group,
            "logStreamName": log_stream,
            "logEvents": [{"timestamp": r[0], "message": r[1]} for r in self._buffer]
        }

        # Send to CW Logs with retry if token has changed
        while True:
            try:
                # add sequence token to API call parms if present
                if self._stream_token:
                    put_event_args["sequenceToken"] = self._stream_token
                resp = get_logs_connection(self.apiclient).put_log_events(**put_event_args)
                self._stream_token = resp.get("nextSequenceToken", None)
                break
            except ClientError as ex:
                exception_type = ex.response['Error']['Code']
                # stream did exist but need new token, get it from exception data
                if exception_type in ["InvalidSequenceTokenException", "DataAlreadyAcceptedException"]:
                    # update the token and retry
                    try:
                        self._stream_token = ex.response['Error']['Message'].split(":")[-1].strip()
                        print("Token changed. Will be retried.")
                        print(("Token for existing stream {} is {}".format(
                            self.stream_name, self._stream_token)))
                    except:
                        self._stream_token = None
                        raise
                else:
                    print(("Error logstream {}, {}".format(self.stream_name, str(ex))))
                    break

        self.clear()
        self._buffer_size = 0

    def clear(self):
        self._buffer = []
        self._buffer_size = 0
