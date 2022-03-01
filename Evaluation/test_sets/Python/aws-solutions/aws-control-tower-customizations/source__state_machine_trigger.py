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

import os
import sys
from utils.logger import Logger
import manifest.manifest_parser as parse
from manifest.sm_execution_manager import SMExecutionManager


def main():
    """
    This function is triggered by CodePipeline stages (ServiceControlPolicy
     and CloudFormationResource). Each stage triggers the following workflow:
     1. Parse the manifest file.
     2. Generate state machine input.
     3. Start state machine execution.
     4. Monitor state machine execution.

     SCP State Machine currently supports parallel deployments only
     Stack Set State Machine currently support sequential deployments only.

    :return: None
    """
    try:
        if len(sys.argv) > 7:

            # set environment variables
            manifest_name = 'manifest.yaml'
            file_path = sys.argv[3]
            os.environ['WAIT_TIME'] = sys.argv[2]
            os.environ['MANIFEST_FILE_PATH'] = file_path
            os.environ['SM_ARN'] = sys.argv[4]
            os.environ['STAGING_BUCKET'] = sys.argv[5]
            os.environ['TEMPLATE_KEY_PREFIX'] = '_custom_ct_templates_staging'
            os.environ['MANIFEST_FILE_NAME'] = manifest_name
            os.environ['MANIFEST_FOLDER'] = file_path[:-len(manifest_name)]
            stage_name = sys.argv[6]
            os.environ['STAGE_NAME'] = stage_name
            os.environ['KMS_KEY_ALIAS_NAME'] = sys.argv[7]
            os.environ['CAPABILITIES'] = '["CAPABILITY_NAMED_IAM","CAPABILITY_AUTO_EXPAND"]'

            sm_input_list = []
            if stage_name.upper() == 'SCP':
                # get SCP state machine input list
                os.environ['EXECUTION_MODE'] = 'parallel'
                sm_input_list = get_scp_inputs()
                logger.info("SCP sm_input_list:")
                logger.info(sm_input_list)

            elif stage_name.upper() == 'STACKSET':
                os.environ['EXECUTION_MODE'] = 'sequential'
                sm_input_list = get_stack_set_inputs()
                logger.info("STACKSET sm_input_list:")
                logger.info(sm_input_list)

            if sm_input_list:
                logger.info("=== Launching State Machine Execution ===")
                launch_state_machine_execution(sm_input_list)
            else:
                logger.info("State Machine input list is empty. No action "
                            "required.")
        else:
            print('No arguments provided. ')
            print('Example: state_machine_trigger.py <LOG-LEVEL> <WAIT_TIME> '
                  '<MANIFEST-FILE-PATH> <SM_ARN_SCP> <STAGING_BUCKET> '
                  '<STAGE-NAME> <KMS_KEY_ALIAS_NAME>')
            sys.exit(2)
    except Exception as e:
        logger.log_unhandled_exception(e)
        raise


def get_scp_inputs() -> list:
    return parse.scp_manifest()


def get_stack_set_inputs() -> list:
    return parse.stack_set_manifest()


def launch_state_machine_execution(sm_input_list):
    if isinstance(sm_input_list, list):
        manager = SMExecutionManager(logger, sm_input_list)
        status, failed_list = manager.launch_executions()

    else:
        raise TypeError("State Machine Input List must be of list type")

    if status == 'FAILED':
        logger.error(
            "\n********************************************************"
            "\nState Machine Execution(s) Failed. \nNavigate to the "
            "AWS Step Functions console \nand review the following "
            "State Machine Executions.\nARN List:\n"
            "{}\n********************************************************"
            .format(failed_list))
        sys.exit(1)


if __name__ == '__main__':
    os.environ['LOG_LEVEL'] = sys.argv[1]
    logger = Logger(loglevel=os.environ['LOG_LEVEL'])
    main()
