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

import urllib
import urllib2
from urllib2 import Request, URLError

from poster.encode import multipart_encode
from api_config import GearpumpApiConfig
import util
import requests
import os


class GearpumpApi:
    def __init__(self, uri, credentials):
        self.gearpump_credentials = credentials
        self.gearpump_uri = uri
        self.gearpump_user_cookies = None
        self.gearpump_app_config = None
        print "Gearpump dashboard uri set to - " + self.gearpump_uri
        print "Gearpump dashboard credentials - " + str(self.gearpump_credentials)

    def __encode_and_prepare_datagen(self, filename):
        datagen, headers = multipart_encode({"file": open(filename, "rb")})
        datagen.params[0].name = 'jar'
        datagen.params[0].filetype = 'application/x-java-archive'
        self.__add_user_cookies_to_headers(headers)
        return datagen, headers

    def __add_user_cookies_to_headers(self, headers):
        if self.gearpump_user_cookies is not None:
            headers['Cookie'] = self.gearpump_user_cookies

    def __create_user_headers_with_cookies(self):
        headers = {}
        self.__add_user_cookies_to_headers(headers)
        return headers

    def __submit_app_jar(self, filename):
        datagen, headers = self.__encode_and_prepare_datagen(filename)

        # Create the Request object
        request_url = self.gearpump_uri + GearpumpApiConfig.call_submit

        files = {
            "args": (None, self.gearpump_app_config, 'application/json'),
            "jar": (os.path.basename(filename), open(filename, "rb"), 'application/x-java-archive')

        }
        print headers

        # Do the request and get the response
        response = requests.post(request_url, files=files, headers=self.__create_user_headers_with_cookies())
        return response

    def __find_active_app_id_by_name(self, name):
        request = Request(self.gearpump_uri + GearpumpApiConfig.call_applist,
                          headers=self.__create_user_headers_with_cookies())

        json = util.call_api(request)

        for app in json['appMasters']:
            if app['appName'] == name and app['status'] == 'active':
                return app['appId']

    def __kill_app(self, app_id):
        request = Request(self.gearpump_uri + GearpumpApiConfig.call_appmaster + "/" + str(app_id),
                          headers=self.__create_user_headers_with_cookies())
        request.get_method = lambda: 'DELETE'
        return util.call_api(request)

    def __get_gearpump_user_cookies(self):
        request_url = self.gearpump_uri + GearpumpApiConfig.call_login
        body = self.gearpump_credentials
        headers = {
            'Content-Type': 'application/x-www-form-urlencoded'
        }

        request = Request(url=request_url, data=urllib.urlencode(body), headers=headers)
        sock = urllib2.urlopen(request)
        cookies = sock.info()['Set-Cookie']
        sock.read()
        sock.close()
        self.gearpump_user_cookies = self.__parse_gearpump_user_cookies(cookies)

    def submit_app(self, filename, app_name, gearpump_app_config=None, force=False):
        print "Gearpump rule engine config - " + str(gearpump_app_config)
        self.gearpump_app_config = util.json_dict_to_string(gearpump_app_config).replace(" ", "")

        if self.gearpump_credentials is not None:
            self.__get_gearpump_user_cookies()

        if force:
            try:
                self.__kill_app(self.__find_active_app_id_by_name(app_name))
            except URLError as e:
                print e.errno
                print app_name + " was not running"
            else:
                print app_name + " was running and got killed"

        self.__submit_app_jar(filename=filename)

        print 'OK'

    def __encode_gearpump_app_config(self, gearpump_app_config):
        return urllib.quote(util.json_dict_to_string(gearpump_app_config).replace(" ", ""))

    def __parse_gearpump_user_cookies(self, cookies):
        return cookies.split(';')[0] + '; username=' + self.gearpump_credentials['username']