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

import com.intel.ruleengine.gearpump.data.RowCreator;
import com.intel.ruleengine.gearpump.tasks.messages.Observation;
import com.intel.ruleengine.gearpump.tasks.messages.Rule;
import org.apache.hadoop.hbase.util.Bytes;


final class RuleComponentsRowCreator implements RowCreator {

    private final String ruleId;
    private final String componentId;

    private static final String ROW_DELIMITER = ":";
    private static final String FIELD_FORMAT_TYPE = "%s";
    private static final String ROW_FORMAT = FIELD_FORMAT_TYPE + ROW_DELIMITER + FIELD_FORMAT_TYPE + ROW_DELIMITER + "%d";

    RuleComponentsRowCreator(ScanProperties scanProperties) {
        this.componentId = scanProperties.getComponentId();
        this.ruleId = scanProperties.getRuleId();
    }

    RuleComponentsRowCreator(Rule rule, Observation observation) {
        this.componentId = observation.getCid();
        this.ruleId = rule.getId();
    }

    public byte[] createRow(long timestamp) {
        return Bytes.toBytes(String.format(ROW_FORMAT, ruleId, componentId, timestamp));
    }

    public static String parseComponentId(byte[] row) {
        String[] rowValues = Bytes.toString(row).split(ROW_DELIMITER);
        return rowValues[1];
    }

    public static String parseRuleId(byte[] row) {
        String[] rowValues = Bytes.toString(row).split(ROW_DELIMITER);
        return rowValues[0];
    }

    public static long parseTimestamp(byte[] row) {
        String[] rowValues = Bytes.toString(row).split(ROW_DELIMITER);
        return Long.parseLong(rowValues[2]);
    }
}
