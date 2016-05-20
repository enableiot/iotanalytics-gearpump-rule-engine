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

import com.intel.ruleengine.gearpump.util.ConfigHelper;
import io.gearpump.cluster.UserConfig;

public final class KerberosProperties {

    public static final String KRB_USER = "hbase.krb.user";
    public static final String KRB_PASS = "hbase.krb.password";
    public static final String KRB_REALM = "hbase.krb.realm";
    public static final String KRB_KDC = "hbase.krb.kdc";
    public static final String KRB_MASTER_PRINCIPAL = "hbase.master.kerberos.principal";
    public static final String KRB_REGIONSERVER_PRINCIPAL = "hbase.regionserver.kerberos.principal";

    private String user;
    private String password;
    private String realm;
    private String kdc;

    private String masterPrincipal;
    private String regionServerPrinicipal;
    private boolean enabled;

    private KerberosProperties() {

    }

    public static KerberosProperties fromConfig(UserConfig config) {
        KerberosProperties kerberosProperties = new KerberosProperties();
        kerberosProperties.enabled = HbaseProperties.KERBEROS_AUTHENTICATION.equals(ConfigHelper.getConfigValue(config, HbaseProperties.AUTHENTICATION_METHOD));
        kerberosProperties.kdc = ConfigHelper.getConfigValue(config, KRB_KDC);
        kerberosProperties.realm = ConfigHelper.getConfigValue(config, KRB_REALM);
        kerberosProperties.user = ConfigHelper.getConfigValue(config, KRB_USER);
        kerberosProperties.password = ConfigHelper.getConfigValue(config, KRB_PASS);
        kerberosProperties.masterPrincipal = ConfigHelper.getConfigValue(config, KRB_MASTER_PRINCIPAL);
        kerberosProperties.regionServerPrinicipal = ConfigHelper.getConfigValue(config, KRB_REGIONSERVER_PRINCIPAL);
        return kerberosProperties;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getRealm() {
        return realm;
    }

    public String getKdc() {
        return kdc;
    }

    public String getMasterPrincipal() {
        return masterPrincipal;
    }

    public String getRegionServerPrinicipal() {
        return regionServerPrinicipal;
    }
}
