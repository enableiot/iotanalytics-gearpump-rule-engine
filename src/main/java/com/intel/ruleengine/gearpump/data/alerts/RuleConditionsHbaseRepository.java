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

package com.intel.ruleengine.gearpump.data.alerts;

import com.intel.ruleengine.gearpump.data.BaseRepository;
import com.intel.ruleengine.gearpump.data.HbaseValues;
import com.intel.ruleengine.gearpump.data.RowCreator;
import com.intel.ruleengine.gearpump.data.RuleConditionsRepository;
import com.intel.ruleengine.gearpump.tasks.messages.Observation;
import com.intel.ruleengine.gearpump.tasks.messages.Rule;
import com.intel.ruleengine.gearpump.tasks.messages.RulesWithObservation;
import io.gearpump.cluster.UserConfig;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class RuleConditionsHbaseRepository extends BaseRepository implements RuleConditionsRepository {

    private static final byte[] FULFILLED_VALUE = Bytes.toBytes("true");
    private static final byte[] NOT_FULFILLED_VALUE = Bytes.toBytes("false");

    private static final byte[] columnFamily = Bytes.toBytes(HbaseValues.RULE_COMPONENTS_COLUMN_FAMILY);
    private static final byte[] ruleComponentsFulfilledColumnName = Bytes.toBytes(HbaseValues.RULE_COMPONENTS_FULFILLED_COLUMN_NAME);
    private static final byte[] timbasedRuleComponentsColumnName = Bytes.toBytes(HbaseValues.TIMEBASED_RULE_COMPONENTS_COLUMN_NAME);

    public RuleConditionsHbaseRepository(UserConfig userConfig) {
        super(userConfig);
    }

    @Override
    protected void addCoprocessor(Admin admin, HTableDescriptor tableDescriptor) throws IOException {
        //No coprocessor required for rulesCondtitons table
    }

    public void putTimebasedRuleComponents(List<RulesWithObservation> rulesWithObservations, boolean isFulfilled) throws IOException {
        byte[] destinationTableName = timbasedRuleComponentsColumnName;
        putRuleComponents(rulesWithObservations, destinationTableName, getRuleComponentValue(isFulfilled));
    }

    public ComponentObservation getLastTimebasedComponentObservation(ScanProperties scanProperties, boolean isFulfilled) throws IOException {
        switchStartStopForReverseScan(scanProperties);
        setDefaultLimitForScan(scanProperties);
        RowCreator rowCreator = new RuleComponentsRowCreator(scanProperties);
        Scan scan = new ScanManager(rowCreator, scanProperties)
                .create(columnFamily, timbasedRuleComponentsColumnName)
                .withReverse()
                .withValueFilter(getRuleComponentValue(isFulfilled))
                .getScan();

        return getComponentObservationFromList(getScanResult(scan));
    }

    public void putFulfilledConditionsForObservation(List<RulesWithObservation> rulesWithObservation) throws IOException {
        putRuleComponents(rulesWithObservation, ruleComponentsFulfilledColumnName, FULFILLED_VALUE);
    }

    public List<ComponentObservation> getFulfilledConditionsForComponent(ScanProperties scanProperties) throws IOException {
        RowCreator rowCreator = new RuleComponentsRowCreator(scanProperties);
        setDefaultLimitForScan(scanProperties);
        Scan scan = new ScanManager(rowCreator, scanProperties).create(columnFamily, ruleComponentsFulfilledColumnName).getScan();
        return getScanResult(scan);
    }

    @Override
    public String getTableNameWithoutPrefix() {
        return HbaseValues.TABLE_NAME;
    }

    private void setDefaultLimitForScan(ScanProperties scanProperties) {
        scanProperties.withLimit(ScanManager.DEFAULT_SCAN_LIMIT);
    }

    private List<ComponentObservation> getScanResult(Scan scan) throws IOException {
        List<ComponentObservation> scanResult = new ArrayList<>();
        try (Table table = getTable(); ResultScanner scanner = table.getScanner(scan)) {
            for (Result result : scanner) {
                scanResult.add(ComponentObservation.fromResultRow(result.getRow()));
            }
        }
        return scanResult;
    }


    private void putRuleComponents(List<RulesWithObservation> rulesWithObservations, byte[] columnName, byte[] value) throws IOException {
        try (Table table = getTable()) {
            List<Put> puts = new ArrayList<>();
            List<Get> gets = new ArrayList<>();
            for (RulesWithObservation rulesWithObservation: rulesWithObservations) {
                for (Rule rule : rulesWithObservation.getRules()) {
                    Get get = new Get(createRow(rule, rulesWithObservation.getObservation()));
                    get.addColumn(columnFamily, columnName);
                    gets.add(get);

                    Put put = new Put(createRow(rule, rulesWithObservation.getObservation()));
                    put.addColumn(columnFamily, columnName, value);
                    puts.add(put);
                /*
                 * this is workaround for single put in gearpump which can be done by:
                 sink.insert(createRow(rule, rulesWithObservation.getObservation()),
                        columnFamily, columnName, value);
                 */
                }
            }
            table.put(puts);
        }
    }

    private static byte[] getRuleComponentValue(boolean isFulfilled) {
        if (isFulfilled) {
            return FULFILLED_VALUE;
        }
        return NOT_FULFILLED_VALUE;
    }

    private static byte[] createRow(Rule rule, Observation observation) {
        return new RuleComponentsRowCreator(rule, observation).createRow(observation.getOn());
    }

    private static ComponentObservation getComponentObservationFromList(List<ComponentObservation> componentObservations) {
        if (componentObservations.size() == 1) {
            return componentObservations.get(0);
        }
        if (componentObservations.isEmpty()) {
            return null;
        }
        throw new IllegalStateException("ComponentObservations list should contain no more than 1 element");
    }

    private void switchStartStopForReverseScan(ScanProperties scanProperties) {
        scanProperties.switchStartStop();
    }
}
