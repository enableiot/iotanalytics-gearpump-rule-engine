package com.intel.ruleengine.gearpump.data.rules;

import com.google.gson.Gson;
import com.intel.ruleengine.gearpump.tasks.messages.Rule;
import com.intel.ruleengine.gearpump.util.LogHelper;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
class RulesRowBuilder {

    private final Logger logger = LogHelper.getLogger(RulesRowBuilder.class);

    private final List<Delete> rowsToDelete;
    private final List<Put> rowsToInsert;

    private final byte[] columnFamily;
    private final byte[] columnName;
    private final Gson gson;
    private Map<String, List<Rule>> existingComponentsRules;
    private Map<String, List<Rule>> newComponentsRules;


    RulesRowBuilder(byte[] columnFamily, byte[] columnName) {
        this.columnFamily = columnFamily;
        this.columnName = columnName;
        rowsToInsert = new ArrayList<>();
        rowsToDelete = new ArrayList<>();
        gson = new Gson();
    }

    public RulesRowBuilder withExistingComponentsRules(Map<String, List<Rule>> componentsRules) {
        this.existingComponentsRules = componentsRules;
        return this;
    }

    public RulesRowBuilder withNewComponentsRules(Map<String, List<Rule>> componentsRules) {
        this.newComponentsRules = componentsRules;
        return this;
    }

    public RulesRowBuilder build() {
        for (String componentId : newComponentsRules.keySet()) {
            byte[] key = createRow(newComponentsRules.get(componentId), componentId);
            List<Rule> activeRules = filterActiveRules(componentId);

            //If there are no active rules for component, remove row connected with it
            if (activeRules.isEmpty()) {
                logger.debug("Will remove rules for component - {}", componentId);
                rowsToDelete.add(new Delete(key));
            } else {
                Put put = new Put(key);
                put.addColumn(columnFamily, columnName, rulesToJson(activeRules));
                rowsToInsert.add(put);
            }
        }

        return this;
    }

    private List<Rule> filterActiveRules(String componentId) {
        RulesDefinitionFilter rulesDefinitionFilter = new RulesDefinitionFilter(existingComponentsRules.get(componentId));
        rulesDefinitionFilter.merge(newComponentsRules.get(componentId).stream());
        return rulesDefinitionFilter.getMergedRulesDefinition();
    }

    private byte[] rulesToJson(List<Rule> rules) {
        return Bytes.toBytes(gson.toJson(rules));
    }

    public List<Delete> getRowsToDelete() {
        return rowsToDelete;
    }

    public List<Put> getRowsToInsert() {
        return rowsToInsert;
    }

    public static byte[] createRow(List<Rule> rules, String componentId) {
        String accountId = rules.get(0).getAccountId();
        return createRow(accountId, componentId);
    }

    public static byte[] createRow(String accountId, String componentId) {
        return new RulesRowCreator(accountId, componentId).createRow();
    }
}
