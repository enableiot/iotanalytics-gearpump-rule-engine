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

package com.intel.ruleengine.gearpump.data.statistics;

import com.intel.ruleengine.gearpump.data.BaseRepository;
import com.intel.ruleengine.gearpump.data.HbaseValues;
import com.intel.ruleengine.gearpump.data.RowCreator;
import com.intel.ruleengine.gearpump.data.StatisticsRepository;
import com.intel.ruleengine.gearpump.data.alerts.ScanManager;
import com.intel.ruleengine.gearpump.data.alerts.ScanProperties;
import com.intel.ruleengine.gearpump.tasks.messages.Observation;
import io.gearpump.cluster.UserConfig;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.coprocessor.DoubleColumnInterpreter;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"checkstyle:illegalcatch", "PMD.AvoidCatchingThrowable"})
public class StatisticsHbaseRepository extends BaseRepository implements StatisticsRepository {

    private static final byte[] columnFamily = Bytes.toBytes(HbaseValues.DATA_COLUMN_FAMILY);
    private static final byte[] columnName = Bytes.toBytes(HbaseValues.DATA_COLUMN_NAME);
    private static final String AGGREGATION_COPROCESSOR_NAME = "org.apache.hadoop.hbase.coprocessor.AggregateImplementation";
    private static final String AGGREGATION_ERR_MSG = "Unable to read statistic data for componentId - %s";


    public StatisticsHbaseRepository(UserConfig userConfig) {
        super(userConfig);
    }

    public double getObservationCount(ScanProperties scanProperties) throws IOException {
        try {
            return getAggregationClient().rowCount(getTableName(), getColumnInterpreter(), getScan(scanProperties));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new IOException(String.format(AGGREGATION_ERR_MSG, scanProperties.getComponentId()), throwable);
        }
    }

    @Override
    public void putObservationForStatisticsRuleCondition(List<Observation> observations) throws IOException {
        try (Table table = getTable()) {
            List<Put> puts = new ArrayList<>();
            for (Observation observation : observations) {
                Put put = new Put(createRow(observation));
                put.addColumn(columnFamily, columnName, getValue(observation));
                puts.add(put);
            }
            table.put(puts);
        } catch (NumberFormatException e) {
            throw new IOException("Incorrect format of observation value", e);
        }
    }

    public StatisticsValues getStatisticsValuesForObservation(ScanProperties scanProperties) throws IOException {
        try {
            CustomAggregationClient aggregationClient = getAggregationClient();
            double avg = aggregationClient.avg(getTableName(), getColumnInterpreter(), getScan(scanProperties));
            double std = aggregationClient.std(getTableName(), getColumnInterpreter(), getScan(scanProperties));
            return new StatisticsValues(avg, std);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new IOException(String.format(AGGREGATION_ERR_MSG, scanProperties.getComponentId()), throwable);
        }
    }

    private DoubleColumnInterpreter getColumnInterpreter() {
        return new DoubleColumnInterpreter();
    }


    private byte[] getValue(Observation observation) {
        return Bytes.toBytes(Double.valueOf(observation.getValue()));
    }

    private byte[] createRow(Observation observation) {
        return new DataRowCreator(observation.getCid()).createRow(observation.getOn());
    }

    private Scan getScan(ScanProperties scanProperties) {
        RowCreator rowCreator = new DataRowCreator(scanProperties);
        return new ScanManager(rowCreator, scanProperties)
                .create(columnFamily, columnName)
                .getScan();
    }

    @Override
    public String getTableNameWithoutPrefix() {
        return HbaseValues.DATA_TABLE_NAME;
    }

    @Override
    protected void addCoprocessor(Admin admin, HTableDescriptor tableDescriptor) throws IOException {
        if (!tableDescriptor.hasCoprocessor(AGGREGATION_COPROCESSOR_NAME)) {
            admin.disableTable(tableDescriptor.getTableName());
            tableDescriptor.addCoprocessor(AGGREGATION_COPROCESSOR_NAME);
            admin.modifyTable(tableDescriptor.getTableName(), tableDescriptor);
            admin.enableTable(tableDescriptor.getTableName());
        }
    }
}
