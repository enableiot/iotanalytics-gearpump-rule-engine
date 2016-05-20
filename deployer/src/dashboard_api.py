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


import requests
import util
from api_config import DashboardApiConfig
import json


class DashboardApi(object):

    def __init__(self, dashboard_url):
        self.dashboard_url = dashboard_url

    def get_token_for_rule_engine_from_dashboard(self, username, password):

        payload = {
            "username": username,
            "password": password
        }

        content_type = {'Content-Type': 'application/json'}

        string_json = util.json_dict_to_string(payload)

        url = self.dashboard_url + "/" + DashboardApiConfig.dashboard_call_auth_token

        response = requests.request('POST', url, data=string_json, headers=content_type, verify=False)

        return self.__parse_token_from_reponse(response)

    def __parse_token_from_reponse(self, response):
        try:
            return json.loads(response.text)['token']
        except:
            raise Exception("Unable to parse Dashboard's token, invalid Dashboard's response - " + str(response))
