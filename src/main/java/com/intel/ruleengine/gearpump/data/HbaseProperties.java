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

public final class HbaseProperties {

    public static final String ZOOKEEPER_QUORUM = "hbase.zookeeper.quorum";
    public static final String TABLE_PREFIX = "hbase.table.prefix";
    public static final String KERBEROS_AUTHENTICATION = "kerberos";
    public static final String AUTHENTICATION_METHOD = "hadoop.security.authentication";
    public static final String HBASE_AUTHENTICATION_METHOD = "hbase.security.authentication";

    private HbaseProperties() {
    }
}
