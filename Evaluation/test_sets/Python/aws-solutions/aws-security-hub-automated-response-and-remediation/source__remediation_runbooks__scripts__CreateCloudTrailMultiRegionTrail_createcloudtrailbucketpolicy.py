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

import json
import boto3
from botocore.config import Config
from botocore.exceptions import ClientError

def connect_to_s3(boto_config):
    return boto3.client('s3', config=boto_config)

def create_bucket_policy(event, context):

    boto_config = Config(
        retries ={
          'mode': 'standard'
        }
    )
    s3 = connect_to_s3(boto_config)

    cloudtrail_bucket = event['cloudtrail_bucket']
    aws_partition = event['partition']
    aws_account = event['account']
    try:
        bucket_policy = {
            "Version": "2012-10-17",
            "Statement": [
                {
                    "Sid": "AWSCloudTrailAclCheck20150319",
                    "Effect": "Allow",
                    "Principal": {
                        "Service": [
                            "cloudtrail.amazonaws.com"
                        ]
                    },
                    "Action": "s3:GetBucketAcl",
                    "Resource": "arn:" + aws_partition + ":s3:::" + cloudtrail_bucket
                },
                {
                    "Sid": "AWSCloudTrailWrite20150319",
                    "Effect": "Allow",
                    "Principal": {
                        "Service": [
                            "cloudtrail.amazonaws.com"
                        ]
                    },
                    "Action": "s3:PutObject",
                    "Resource": "arn:" + aws_partition + ":s3:::" + cloudtrail_bucket + "/AWSLogs/" + aws_account + "/*",
                    "Condition": { 
                        "StringEquals": { 
                            "s3:x-amz-acl": "bucket-owner-full-control"
                        }
                    }
                }
            ]
        }
        s3.put_bucket_policy(
            Bucket=cloudtrail_bucket,
            Policy=json.dumps(bucket_policy)
        )
        return {
            "output": {
                "Message": f'Set bucket policy for bucket {cloudtrail_bucket}'
            }
        }
    except Exception as e:
        print(e)
        exit('PutBucketPolicy failed: ' + str(e))
