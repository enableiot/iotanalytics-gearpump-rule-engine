package com.intel.ruleengine.gearpump.graph;

import com.google.gson.Gson;
import com.intel.ruleengine.gearpump.apiclients.DashboardConfig;
import com.intel.ruleengine.gearpump.data.HbaseProperties;
import com.intel.ruleengine.gearpump.data.KerberosProperties;
import com.intel.ruleengine.gearpump.tasks.KafkaSourceProcessor;
import com.intel.ruleengine.gearpump.util.LogHelper;
import io.gearpump.cluster.UserConfig;
import org.slf4j.Logger;

/**
 * Copyright (c) 2016 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class GraphConfig {
    private final UserConfig config;
    private final Config externalConfig;

    public GraphConfig(String... args) {

        externalConfig = getConfigFromArgs(args[0]);

        Logger logger = LogHelper.getLogger(GraphConfig.class);
        logger.info("configuration: {}", externalConfig);

        config = UserConfig.empty()
                .withString(HbaseProperties.ZOOKEEPER_QUORUM, externalConfig.getZookeeper_hbase_quorum())
                .withString(HbaseProperties.TABLE_PREFIX, externalConfig.getHbase_table_prefix())
                .withString(DashboardConfig.DASHBOARD_TOKEN_PROPERTY, externalConfig.getToken())
                .withString(DashboardConfig.DASHBOARD_URL_PROPERTY, externalConfig.getDashboard_url())
                .withBoolean(DashboardConfig.DASHBOARD_STRICT_SSL_VERIFICATION, externalConfig.getDashboard_strict_ssl())
                .withString(KafkaSourceProcessor.KAFKA_TOPIC_PROPERTY, externalConfig.getKafka_topic())
                .withString(KafkaSourceProcessor.KAFKA_URI_PROPERTY, externalConfig.getKafka_servers())
                .withString(KafkaSourceProcessor.KAFKA_ZOOKEEPER_PROPERTY, externalConfig.getKafka_zookeeper_quorum())
                .withString(HbaseProperties.AUTHENTICATION_METHOD, externalConfig.getHadoop_security_authentication())
                .withString(KerberosProperties.KRB_KDC, externalConfig.getKrb_kdc())
                .withString(KerberosProperties.KRB_PASS, externalConfig.getKrb_password())
                .withString(KerberosProperties.KRB_REALM, externalConfig.getKrb_realm())
                .withString(KerberosProperties.KRB_USER, externalConfig.getKrb_user())
                .withString(KerberosProperties.KRB_MASTER_PRINCIPAL, externalConfig.getKrb_master_principal())
                .withString(KerberosProperties.KRB_REGIONSERVER_PRINCIPAL, externalConfig.getKrb_regionserver_principal());
    }

    private Config getConfigFromArgs(String args) {
        Gson g = new Gson();
        return g.fromJson(args, Config.class);
    }

    public UserConfig getConfig() {
        return config;
    }

    public String getGraphApplicationName() {
        return externalConfig.getApplication_name();
    }
}
