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

package com.intel.ruleengine.gearpump.rules;


public enum ConditionOperators {
    AND("AND"), OR("OR"), NONE("NONE");

    private String stringValue;

    ConditionOperators(String value) {
        this.stringValue = value;
    }

    public static ConditionOperators fromString(String value) {
        if (value == null) {
            return NONE;
        }
        for (ConditionOperators op : ConditionOperators.values()) {
            if (op.stringValue.equals(value)) {
                return op;
            }
        }
        throw new IllegalArgumentException("Unrecognized operator value provided - " + value);
    }
}