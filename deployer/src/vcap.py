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

import os

import util
import dashboard_api


class VcapType:
    services = 'VCAP_SERVICES'
    application = 'VCAP_APPLICATION'


class Vcap(object):

    def __init__(self, vtype):
        self.json = util.json_string_to_dict(os.environ[vtype])


class VcapServices(Vcap):

    def __init__(self):
        super(VcapServices, self).__init__(VcapType.services)
        self.__parse_zookeeper_hbase_quorum()
        self.__parse_kafka_servers()
        self.__parse_kafka_zookeeper_quorum()

        ups = self.json['user-provided']

        self.__parse_dashboard_url(ups)
        self.__gather_rule_engine_token(ups)
        self.__gather_topic_name(ups)
        self.__parse_gearpump_credentials()
        self.__parse_kerberos_hbase_properties(ups)

    def __gather_rule_engine_token(self, ups):
        rule_engine_ups = self.__get_ups_by_name(ups, 'rule-engine-credentials-ups')
        username = rule_engine_ups['credentials']['username']
        password = rule_engine_ups['credentials']['password']

        dashboard_pipe = dashboard_api.DashboardApi(self.dashboard_url)

        self.token = dashboard_pipe.get_token_for_rule_engine_from_dashboard(username, password)

    def __parse_dashboard_url(self, ups):
        dashboard_endpoint_ups = self.__get_ups_by_name(ups, 'dashboard-endpoint-ups')['credentials']
        self.dashboard_url = dashboard_endpoint_ups['host']
        if 'strictSSL' in dashboard_endpoint_ups:
            self.dashboard_strict_ssl = dashboard_endpoint_ups['strictSSL']
        else:
            self.dashboard_strict_ssl = True

        self.dashboard_url_normalized_for_gearpump = self.dashboard_url \
            .replace("http://", "") \
            .replace("https://", "") \
            .replace("-", "_") \
            .replace(".", "_")

    def __gather_topic_name(self, ups):
        kafka_ups = self.__get_ups_by_name(ups, 'kafka-ups')
        self.topic_name = kafka_ups['credentials']['topic']

    def __parse_gearpump_credentials(self):
        self.gearpump_credentials = self.json['gearpump'][0]['credentials']
        self.gearpump_url = self.gearpump_credentials['dashboardUrl']
        if 'username' in self.gearpump_credentials:
            self.gearpump_password = self.gearpump_credentials['password']
            self.gearpump_username = self.gearpump_credentials['username']

    def __parse_zookeeper_hbase_quorum(self):
        self.zookeeper_hbase_quorum = self.json['hbase'][0]['credentials']['HADOOP_CONFIG_KEY']['ha.zookeeper.quorum']

    def __parse_kerberos_hbase_properties(self, ups):
        hbase_config = self.json['hbase'][0]['credentials']['HADOOP_CONFIG_KEY']

        self.hadoop_security_authentication = hbase_config['hadoop.security.authentication']
        if self.is_kerberos_enabled():
            self.krb_master_principal = hbase_config['hbase.master.kerberos.principal']
            self.krb_regionserver_principal = hbase_config['hbase.regionserver.kerberos.principal']
        kerberos_service = self.json['kerberos'][0]['credentials']
        self.krb_kdc = kerberos_service['kdc']
        self.krb_password = kerberos_service['kpassword']
        self.krb_user = kerberos_service['kuser']
        self.krb_realm = kerberos_service['krealm']


    def is_kerberos_enabled(self):
        return self.hadoop_security_authentication == 'kerberos'

    def __parse_kafka_servers(self):
        self.kafka_servers = self.json['kafka'][0]['credentials']['uri']

    def __parse_kafka_zookeeper_quorum(self):
        self.kafka_zookeeper_quorum = self.json['zookeeper'][0]['credentials']['zk.cluster']

    @staticmethod
    def __get_ups_by_name(json_ups, name):
        for credential_set in json_ups:
            if credential_set['name'] == name:
                return credential_set
        raise "Couldn't find credentials named " + name


class VcapApplication(Vcap):
    def __init__(self):
        super(VcapApplication, self).__init__(VcapType.application)
        self.space_name = self.json['space_name']
