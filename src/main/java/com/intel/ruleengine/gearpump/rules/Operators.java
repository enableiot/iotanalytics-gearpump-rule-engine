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


import com.intel.ruleengine.gearpump.rules.conditions.functions.Functions;

import java.util.List;

public enum Operators {

    EQUAL("Equal") {
        public boolean isFulfilled(Functions functions, String value, List<String> values) {
            return functions.equal(value, values);
        }
    },
    NOT_EQUAL("Not Equal") {
        public boolean isFulfilled(Functions functions, String value, List<String> values) {
            return functions.notEqual(value, values);
        }
    },
    LIKE("Like") {
        public boolean isFulfilled(Functions functions, String value, List<String> values) {
            return functions.like(value, values.get(0));
        }
    },
    GREATER(">") {
        public boolean isFulfilled(Functions functions, String value, List<String> values) {
            return functions.grater(value, values.get(0));
        }
    },
    GREATER_EQ(">=") {
        public boolean isFulfilled(Functions functions, String value, List<String> values) {
            return functions.graterEq(value, values.get(0));
        }
    },
    LOWER("<") {
        public boolean isFulfilled(Functions functions, String value, List<String> values) {
            return functions.lower(value, values.get(0));
        }
    },
    LOWER_EQ("<=") {
        public boolean isFulfilled(Functions functions, String value, List<String> values) {
            return functions.lowerEq(value, values.get(0));
        }
    },
    BETWEEN("Between") {
        public boolean isFulfilled(Functions functions, String value, List<String> values) {
            return functions.between(value, values);
        }
    },
    NOT_BETWEEN("Not Between") {
        public boolean isFulfilled(Functions functions, String value, List<String> values) {
            return functions.notBetween(value, values);
        }
    };

    private String stringValue;

    Operators(String stringValue) {
        this.stringValue = stringValue;
    }

    public abstract boolean isFulfilled(Functions functions, String value, List<String> values);

    public static Operators fromString(String value) {
        for (Operators op : Operators.values()) {
            if (op.stringValue.equals(value)) {
                return op;
            }
        }
        throw new IllegalArgumentException("Unrecognized operator value provided - " + value);
    }
}
