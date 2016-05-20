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

import com.intel.ruleengine.gearpump.data.statistics.CustomAggregationClient;
import io.gearpump.cluster.UserConfig;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Table;

import javax.annotation.PreDestroy;
import java.io.IOException;

public abstract class BaseRepository {

    private final String tableName;
    private final String zkQuorum;
    private final UserConfig userConfig;
    private Connection hbaseConnection;

    public BaseRepository(UserConfig userConfig) {
        String tablePrefix = userConfig.getString(HbaseProperties.TABLE_PREFIX).get();
        tableName = buildTableName(tablePrefix);
        zkQuorum = userConfig.getString(HbaseProperties.ZOOKEEPER_QUORUM).get();
        this.userConfig = userConfig;
    }

    public void createTable() throws IOException {
        try (Admin admin = getHbaseAdmin()) {
            if (!admin.tableExists(getTableName())) {
                HTableDescriptor table = new HTableDescriptor(getTableName());
                for (String family : HbaseValues.TABLES_COLUMN_FAMILIES.get(getTableNameWithoutPrefix())) {
                    table.addFamily(new HColumnDescriptor(family));
                }
                admin.createTable(table);
                addCoprocessor(admin, table);
            }
        }
    }

    protected abstract void addCoprocessor(Admin admin, HTableDescriptor tableDescriptor) throws IOException;

    private String buildTableName(String prefix) {
        return new StringBuilder(prefix).append(getTableNameWithoutPrefix()).toString();
    }

    public TableName getTableName() {
        return TableName.valueOf(tableName);
    }

    public abstract String getTableNameWithoutPrefix();

    private Connection getHbaseConnection() throws IOException {
        if (hbaseConnection == null || hbaseConnection.isClosed()) {
            hbaseConnection =  HbaseConnManager.newInstance(KerberosProperties.fromConfig(userConfig), zkQuorum).create();
        }
        return hbaseConnection;
    }

    @PreDestroy
    protected void closeConnection() throws IOException {
        if (hbaseConnection != null && !hbaseConnection.isClosed()) {
            hbaseConnection.close();
        }
    }

    protected Table getTable() throws IOException {
        return getHbaseConnection().getTable(getTableName());
    }

    protected Admin getHbaseAdmin() throws IOException {
        return getHbaseConnection().getAdmin();
    }

    protected CustomAggregationClient getAggregationClient() throws IOException {
        return new CustomAggregationClient(getHbaseConnection());
    }
}
