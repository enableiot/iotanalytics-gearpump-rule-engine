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

from urllib2 import urlopen
from poster.streaminghttp import register_openers
from json import JSONEncoder, JSONDecoder

# Register the streaming http handlers with urllib2
register_openers()


def call_api(request):

    response = urlopen(request)
    responsestr = response.read()
    return json_string_to_dict(responsestr)


def json_string_to_dict(json_as_string):
    return JSONDecoder().decode(json_as_string)


def json_dict_to_string(json_as_dict):
    return JSONEncoder().encode(json_as_dict)