###############################################################################
#  Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.    #
#                                                                             #
#  Licensed under the Apache License, Version 2.0 (the "License").            #
#  You may not use this file except in compliance with the License.
#  A copy of the License is located at                                        #
#                                                                             #
#      http://www.apache.org/licenses/LICENSE-2.0                             #
#                                                                             #
#  or in the "license" file accompanying this file. This file is distributed  #
#  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express #
#  or implied. See the License for the specific language governing permissions#
#  and limitations under the License.                                         #
###############################################################################


def transform_params(params_in):
    """Splits input parameter into parameter key and value.
    Args:
        params_in (dict): Python dict of input params e.g.
        {
            "principal_role": "$[alfred_ssm_/org/primary/service_catalog/
            principal/role_arn]"
        }

    Return:
        params_out (list): Python list of output params e.g.
        {
            "ParameterKey": "principal_role",
            "ParameterValue": "$[alfred_ssm_/org/primary/service_catalog/
            principal/role_arn]"
        }
    """
    params_list = []
    for key, value in params_in.items():
        param = {}
        param.update({"ParameterKey": key})
        param.update({"ParameterValue": value})
        params_list.append(param)
    return params_list


def reverse_transform_params(params_in):
    """Merge input parameter key and value into one-line string
    Args:
        params_in (list): Python list of output params e.g.
        {
            "ParameterKey": "principal_role",
            "ParameterValue": "$[alfred_ssm_/org/primary/service_catalog/
            principal/role_arn]"
        }
    Return:
        params_out (dict): Python dict of input params e.g.
        {
            "principal_role": "$[alfred_ssm_/org/primary/service_catalog/
            principal/role_arn]"
        }
    """
    params_out = {}
    for param in params_in:
        key = param.get("ParameterKey")
        value = param.get("ParameterValue")
        params_out.update({key: value})
    return params_out
