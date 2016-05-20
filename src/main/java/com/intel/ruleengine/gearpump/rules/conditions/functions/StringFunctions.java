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

package com.intel.ruleengine.gearpump.rules.conditions.functions;

import java.util.List;

class StringFunctions implements Functions {
    @Override
    public boolean equal(String value1, List<String> values) {
        for (String value : values) {
            if (value.equals(value1)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean notEqual(String value1, List<String> values) {
        return !equal(value1, values);
    }

    @Override
    public boolean grater(String value1, String value2) {
        throw new IllegalOperatorException("Grater operator is not supported for string data type");
    }

    @Override
    public boolean graterEq(String value1, String value2) {
        throw new IllegalOperatorException("GraterEq operator is not supported for string data type");
    }

    @Override
    public boolean lowerEq(String value1, String value2) {
        throw new IllegalOperatorException("LowerEq operator is not supported for string data type");
    }

    @Override
    public boolean lower(String value1, String value2) {
        throw new IllegalOperatorException("Lower operator is not supported for string data type");
    }

    @Override
    public boolean like(String value1, String value2) {
        return value2.contains(value1);
    }

    @Override
    public boolean between(String value1, List<String> value2) {
        throw new IllegalOperatorException("Between operator is not supported for string data type");
    }

    @Override
    public boolean notBetween(String value1, List<String> value2) {
        throw new IllegalOperatorException("Not Between operator is not supported for string data type");
    }
}
