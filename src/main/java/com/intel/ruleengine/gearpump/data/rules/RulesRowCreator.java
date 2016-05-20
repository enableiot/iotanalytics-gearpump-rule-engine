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

import org.apache.hadoop.hbase.util.Bytes;


final class RulesRowCreator {

    private final String componentId;
    private final String accountId;

    private static final String ROW_DELIMITER = ":";
    private static final String FIELD_FORMAT_TYPE = "%s";
    private static final String ROW_FORMAT = FIELD_FORMAT_TYPE + ROW_DELIMITER + FIELD_FORMAT_TYPE;

    RulesRowCreator(String accountId, String componentId) {
        this.accountId = accountId;
        this.componentId = componentId;
    }

    public byte[] createRow() {
        return Bytes.toBytes(String.format(ROW_FORMAT, accountId, componentId));
    }

    public static String parseComponentId(byte[] row) {
        String[] rowValues = Bytes.toString(row).split(ROW_DELIMITER);
        return rowValues[1];
    }

    public static String parseAccountId(byte[] row) {
        String[] rowValues = Bytes.toString(row).split(ROW_DELIMITER);
        return rowValues[0];
    }
}
