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

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public final class HbaseValues {

    public static final String DATA_TABLE_NAME = "_OBSERVATIONS_DATA";
    public static final String DATA_COLUMN_FAMILY = "data_for_statistics_conditions";
    public static final String DATA_COLUMN_NAME = "val";

    public static final String TABLE_NAME = "_RULE_ENGINE";
    public static final String RULE_COMPONENTS_COLUMN_FAMILY = "rule_components";
    public static final String RULE_COMPONENTS_FULFILLED_COLUMN_NAME = "rule_fulfilled_components";

    public static final String TIMEBASED_RULE_COMPONENTS_COLUMN_NAME = "timebased_rule_components";

    public static final String RULES_COLUMN_FAMILY = "rules";
    public static final String RULES_COLUMN_NAME = "rules_active";

    public static final Map<String, String[]> TABLES_COLUMN_FAMILIES =
            ImmutableMap.of(TABLE_NAME, new String[]{RULE_COMPONENTS_COLUMN_FAMILY, RULES_COLUMN_FAMILY},
                    DATA_TABLE_NAME, new String[]{DATA_COLUMN_FAMILY});

    private HbaseValues() {
    }
}
