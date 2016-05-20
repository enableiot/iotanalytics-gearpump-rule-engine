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

class NumberFunctions implements Functions {

    @Override
    public boolean equal(String value1, List<String> values) {
        for (String value : values) {
            if (Double.compare(Double.valueOf(value), Double.valueOf(value1)) == 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean grater(String value1, String value2) {
        return Double.compare(Double.valueOf(value1), Double.valueOf(value2)) > 0;
    }

    @Override
    public boolean graterEq(String value1, String value2) {
        return Double.compare(Double.valueOf(value1), Double.valueOf(value2)) >= 0;
    }

    @Override
    public boolean lowerEq(String value1, String value2) {
        return Double.compare(Double.valueOf(value1), Double.valueOf(value2)) <= 0;
    }

    public boolean lower(String value1, String value2) {
        return Double.compare(Double.valueOf(value1), Double.valueOf(value2)) < 0;
    }

    @Override
    public boolean like(String value1, String value2) {
        throw new IllegalOperatorException("Like operator is not supported for number data type");
    }

    @Override
    public boolean between(String value1, List<String> value2) {
        return graterEq(value1, value2.get(0)) && lowerEq(value1, value2.get(1));
    }

    @Override
    public boolean notBetween(String value1, List<String> value2) {
        return lower(value1, value2.get(0)) || grater(value1, value2.get(1));
    }

    public boolean notEqual(String value1, List<String> values) {
        return !equal(value1, values);
    }
}
