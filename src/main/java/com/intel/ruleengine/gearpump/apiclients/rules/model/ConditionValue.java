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

package com.intel.ruleengine.gearpump.apiclients.rules.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConditionValue {

    private String type;
    private String operator;
    private List<String> values;
    private ConditionComponent component;
    private Long timeLimit;
    private Long baselineSecondsBack;
    private Long baselineMinimalInstances;

    public Long getBaselineSecondsBack() {
        return baselineSecondsBack;
    }

    public void setBaselineSecondsBack(Long baselineSecondsBack) {
        this.baselineSecondsBack = baselineSecondsBack;
    }

    public Long getBaselineMinimalInstances() {
        return baselineMinimalInstances;
    }

    public void setBaselineMinimalInstances(Long baselineMinimalInstances) {
        this.baselineMinimalInstances = baselineMinimalInstances;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public ConditionComponent getComponent() {
        return component;
    }

    public void setComponent(ConditionComponent component) {
        this.component = component;
    }

    public Long getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(Long timeLimit) {
        this.timeLimit = timeLimit;
    }
}
