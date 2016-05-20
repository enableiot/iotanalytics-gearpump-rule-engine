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


class DashboardApiConfig(object):
    dashboard_api_version = "v1"
    dashboard_call_auth_token = dashboard_api_version + "/api/auth/token/"


class GearpumpApiConfig(object):
    version = "v1.0"
    prefix = "/api/" + version
    prefix_master = prefix + "/master/"
    call_login = "/login"
    call_submit = prefix_master + "submitapp"
    call_submit_with_args = call_submit + "?args="
    call_applist = prefix_master + "applist"
    call_appmaster = prefix + "/appmaster"