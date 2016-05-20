/*
 * Copyright (c) 2016 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.intel.ruleengine.gearpump.data;

import com.intel.ruleengine.gearpump.util.LogHelper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.security.User;
import org.apache.hadoop.hbase.security.UserProvider;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.trustedanalytics.hadoop.kerberos.KrbLoginManager;
import org.trustedanalytics.hadoop.kerberos.KrbLoginManagerFactory;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import java.io.IOException;

final class HbaseConnManager {

    private final KerberosProperties kerberosProperties;
    private final Configuration hbaseConfiguration;
    private static final Logger logger = LogHelper.getLogger(HbaseConnManager.class);

    private HbaseConnManager(KerberosProperties kerberosProperties, String zkQuorum) {
        this(kerberosProperties, createHbaseConfiguration(zkQuorum, kerberosProperties));
    }

    private HbaseConnManager(KerberosProperties kerberosProperties, Configuration hbaseConfiguration) {
        this.hbaseConfiguration = hbaseConfiguration;
        this.kerberosProperties = kerberosProperties;
    }

    public static HbaseConnManager newInstance(KerberosProperties kerberosProperties, String zkQuorum) {
        return new HbaseConnManager(kerberosProperties, zkQuorum);
    }

    public Connection create() throws IOException {
        if (kerberosProperties.isEnabled()) {
            logger.info("Creating hbase connection with kerberos auth");
            try {
                KrbLoginManager loginManager = KrbLoginManagerFactory.getInstance()
                        .getKrbLoginManagerInstance(kerberosProperties.getKdc(), kerberosProperties.getRealm());
                Subject subject = loginManager.loginWithCredentials(kerberosProperties.getUser(),
                        kerberosProperties.getPassword().toCharArray());
                loginManager.loginInHadoop(subject, hbaseConfiguration);

                return ConnectionFactory.createConnection(hbaseConfiguration, getUserFromSubject(subject));
            } catch (LoginException e) {
                logger.error("Create hbase connection failed. Unable to authorize with kerberos credentials: "
                        + "user - {}, realm - {}, kdc - {}", kerberosProperties.getUser(), kerberosProperties.getRealm(), kerberosProperties.getKdc());
                throw new IOException(e);
            }
        } else {
            return ConnectionFactory.createConnection(hbaseConfiguration, getNoKrbUserFromSubject(hbaseConfiguration, kerberosProperties.getUser()));
        }

    }

    private User getUserFromSubject(Subject subject) throws IOException {
        return UserProvider.instantiate(hbaseConfiguration)
                .create(UserGroupInformation.getUGIFromSubject(subject));
    }

    private User getNoKrbUserFromSubject(Configuration configuration, String krbUser) throws IOException {
        return UserProvider.instantiate(configuration)
                .create(UserGroupInformation.createRemoteUser(krbUser));
    }

    private static Configuration createHbaseConfiguration(String zkQuorum, KerberosProperties kerberosProperties) {
        Configuration hbaseConfig = HBaseConfiguration.create();

        if (kerberosProperties.isEnabled()) {
            hbaseConfig.set(HbaseProperties.AUTHENTICATION_METHOD, HbaseProperties.KERBEROS_AUTHENTICATION);
            hbaseConfig.set(HbaseProperties.HBASE_AUTHENTICATION_METHOD, HbaseProperties.KERBEROS_AUTHENTICATION);
            hbaseConfig.set(KerberosProperties.KRB_MASTER_PRINCIPAL, kerberosProperties.getMasterPrincipal());
            hbaseConfig.set(KerberosProperties.KRB_REGIONSERVER_PRINCIPAL, kerberosProperties.getRegionServerPrinicipal());
        }

        hbaseConfig.set(HbaseProperties.ZOOKEEPER_QUORUM, zkQuorum);

        return hbaseConfig;
    }
}
