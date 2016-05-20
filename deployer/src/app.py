# Copyright (c) 2015 Intel Corporation
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


import argparse
import os

import time

import cloudfoundry_bridge
from gearpump_api import GearpumpApi


def main():

    parser = argparse.ArgumentParser()
    parser.add_argument("--local", action="store_true")
    args = parser.parse_args()

    cloud_bridge = cloudfoundry_bridge.CloudfoundryBridge()

    config = cloud_bridge.build_config(local=args.local)

    gearpump_api = GearpumpApi(uri=cloud_bridge.gearpump_dashboard_url, credentials=cloud_bridge.gearpump_credentials)

    rule_engine_jar_name = os.environ['RULE_ENGINE_PACKAGE_NAME']

    print 'Submitting application - %s into gearpump ...' % rule_engine_jar_name

    gearpump_api.submit_app(filename=rule_engine_jar_name, app_name=config['application_name'],
                             gearpump_app_config=config, force=True)

    if not args.local:
        while True:
            time.sleep(60)

if __name__ == "__main__":
    main()
