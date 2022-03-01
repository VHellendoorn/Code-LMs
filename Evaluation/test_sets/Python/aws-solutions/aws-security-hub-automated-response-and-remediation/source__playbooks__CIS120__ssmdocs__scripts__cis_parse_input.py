#!/usr/bin/python
###############################################################################
#  Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.         #
#                                                                             #
#  Licensed under the Apache License Version 2.0 (the "License"). You may not #
#  use this file except in compliance with the License. A copy of the License #
#  is located at                                                              #
#                                                                             #
#      http://www.apache.org/licenses/LICENSE-2.0/                            #
#                                                                             #
#  or in the "license" file accompanying this file. This file is distributed  #
#  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express #
#  or implied. See the License for the specific language governing permis-    #
#  sions and limitations under the License.                                   #
###############################################################################
import re

def get_control_id_from_arn(finding_id_arn):
    check_finding_id = re.match(
        '^arn:(?:aws|aws-cn|aws-us-gov):securityhub:(?:[a-z]{2}(?:-gov)?-[a-z]+-\\d):\\d{12}:subscription/cis-aws-foundations-benchmark/v/1\\.2\\.0/(.*)/finding/(?i:[0-9a-f]{8}-(?:[0-9a-f]{4}-){3}[0-9a-f]{12})$',
        finding_id_arn
    )
    if check_finding_id:
        control_id = check_finding_id.group(1)
        return control_id
    else:
        exit(f'ERROR: Finding Id is invalid: {finding_id_arn}')

def parse_event(event, context):
    expected_control_id = event['expected_control_id']
    parse_id_pattern = event['parse_id_pattern']
    resource_id_matches = []
    finding = event['Finding']
    testmode = bool('testmode' in finding)

    finding_id = finding['Id']
        
    account_id = finding.get('AwsAccountId', '')
    if not re.match('^\\d{12}$', account_id):
        exit(f'ERROR: AwsAccountId is invalid: {account_id}')

    control_id = get_control_id_from_arn(finding['Id'])

    # ControlId present and valid
    if not control_id:
        exit(f'ERROR: Finding Id is invalid: {finding_id} - missing Control Id')

    # ControlId is the expected value
    if control_id not in expected_control_id:
        exit(f'ERROR: Control Id from input ({control_id}) does not match {str(expected_control_id)}')

    # ProductArn present and valid
    product_arn = finding['ProductArn']
    if not re.match('^arn:(?:aws|aws-cn|aws-us-gov):securityhub:(?:[a-z]{2}(?:-gov)?-[a-z]+-\\d)::product/aws/securityhub$', product_arn):
        exit(f'ERROR: ProductArn is invalid: {product_arn}')

    resource = finding['Resources'][0]

    # Details
    details = finding['Resources'][0].get('Details', {})

    # Regex match Id to get remediation-specific identifier
    identifier_raw = finding['Resources'][0]['Id']
    resource_id = identifier_raw

    if parse_id_pattern:
        identifier_match = re.match(
            parse_id_pattern,
            identifier_raw
        )

        if identifier_match:
            for group in range(1, len(identifier_match.groups())+1):
                resource_id_matches.append(identifier_match.group(group))
            resource_id = identifier_match.group(event.get('resource_index', 1))
        else:
            exit(f'ERROR: Invalid resource Id {identifier_raw}')   

    if not resource_id:
        exit('ERROR: Resource Id is missing from the finding json Resources (Id)')

    affected_object = {'Type': resource['Type'], 'Id': resource_id, 'OutputKey': 'Remediation.Output'}
    return {
        "account_id": account_id,
        "resource_id": resource_id, 
        "finding_id": finding_id, 
        "control_id": control_id,
        "product_arn": product_arn, 
        "object": affected_object,
        "matches": resource_id_matches,
        "details": details,
        "testmode": testmode,
        "resource": resource
    }