package com.intel.ruleengine.gearpump.rules;

import java.util.ArrayList;
import java.util.List;

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
public enum RuleStatus {

    ACTIVE("Active"), DELETED("Deleted"), ARCHIVED("Archived"), ON_HOLD("On-hold");

    private String stringValue;

    RuleStatus(String stringValue) {
        this.stringValue = stringValue;
    }

    @Override
    public String toString() {
        return stringValue;
    }

    public static List<String> asList() {
        List<String> statusNames = new ArrayList<>();
        for (RuleStatus status: values()) {
            statusNames.add(status.toString());
        }
        return statusNames;
    }

    public static RuleStatus fromString(String value) {
        for (RuleStatus status : RuleStatus.values()) {
            if (status.stringValue.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unrecognized rule status provided - " + value);
    }
}
