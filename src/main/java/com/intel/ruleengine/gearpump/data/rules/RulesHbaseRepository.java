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

package com.intel.ruleengine.gearpump.data.rules;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intel.ruleengine.gearpump.data.BaseRepository;
import com.intel.ruleengine.gearpump.data.HbaseValues;
import com.intel.ruleengine.gearpump.data.RulesRepository;
import com.intel.ruleengine.gearpump.tasks.messages.Rule;
import io.gearpump.cluster.UserConfig;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class RulesHbaseRepository extends BaseRepository implements RulesRepository {

    private static final byte[] columnFamily = Bytes.toBytes(HbaseValues.RULES_COLUMN_FAMILY);
    private static final byte[] columnName = Bytes.toBytes(HbaseValues.RULES_COLUMN_NAME);
    private final Gson gson;

    public RulesHbaseRepository(UserConfig userConfig) {
        super(userConfig);
        gson = new Gson();
    }

    @Override
    protected void addCoprocessor(Admin admin, HTableDescriptor tableDescriptor) throws IOException {
        //No coprocessor required for rules table
    }

    public void putRulesAndRemoveNotExistingOnes(Map<String, List<Rule>> componentsRules) throws IOException {
        try (Table table = getTable()) {
            FilterList filters = createRuleKeyFilter(getAccountIdFromMap(componentsRules), componentsRules.keySet());

            RulesRowBuilder rulesRowBuilder = new RulesRowBuilder(columnFamily, columnName)
                    .withExistingComponentsRules(getExistingComponentRules(table, filters))
                    .withNewComponentsRules(componentsRules)
                    .build();

            table.delete(rulesRowBuilder.getRowsToDelete());
            table.put(rulesRowBuilder.getRowsToInsert());
        }
    }

    private String getAccountIdFromMap(Map<String, List<Rule>> componentsRules) {
        if (!componentsRules.isEmpty()) {
            List<Rule> rules = componentsRules.values().iterator().next();
            if (!rules.isEmpty()) {
                return rules.get(0).getAccountId();
            }
        }
        return null;
    }

    private FilterList createRuleKeyFilter(String accountId, Set<String> componentsIds) {
        List<Filter> filters = new ArrayList<>();
        for (String componentId : componentsIds) {
            byte[] key = RulesRowBuilder.createRow(accountId, componentId);
            // filter to find existing definition of components rules
            filters.add(new RowFilter(CompareFilter.CompareOp.EQUAL, new BinaryComparator(key)));
        }

        return new FilterList(FilterList.Operator.MUST_PASS_ONE, filters);
    }

    private Map<String, List<Rule>> getExistingComponentRules(Table table, FilterList filters) throws IOException {
        Map<String, List<Rule>> scanResult = new HashMap<>();
        Scan scan = new Scan().addColumn(columnFamily, columnName).setFilter(filters);

        try (ResultScanner scanner = table.getScanner(scan)) {
            for (Result result : scanner) {
                byte[] rules = result.getValue(columnFamily, columnName);
                byte[] row = result.getRow();

                String componentId = RulesRowCreator.parseComponentId(row);
                scanResult.put(componentId, jsonToRules(rules));
            }
        }

        return scanResult;
    }

    @Override
    public Map<String, List<Rule>> getComponentsRules(String accountId, Set<String> componentsIds) throws IOException {
        FilterList filters = createRuleKeyFilter(accountId, componentsIds);

        try (Table table = getTable()) {
            return getExistingComponentRules(table, filters);
        }
    }

    @Override
    public String getTableNameWithoutPrefix() {
        return HbaseValues.TABLE_NAME;
    }

    private List<Rule> jsonToRules(byte[] result) {
        Type type = new TypeToken<List<Rule>>() { } .getType();
        return gson.fromJson(Bytes.toString(result), type);
    }
}
