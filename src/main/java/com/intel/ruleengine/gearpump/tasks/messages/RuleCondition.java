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

package com.intel.ruleengine.gearpump.tasks.messages;

import com.google.common.base.Objects;
import com.intel.ruleengine.gearpump.apiclients.rules.model.ConditionValue;
import com.intel.ruleengine.gearpump.rules.ConditionType;
import com.intel.ruleengine.gearpump.rules.DataType;
import com.intel.ruleengine.gearpump.rules.Operators;

import java.util.List;


public class RuleCondition {

    private List<String> values;
    private Operators operator;
    private ConditionType type;
    private String componentId;
    private DataType componentDataType;
    /**
     * timeLimit for timebased and statistics conditions (in seconds)
     */
    private Long timeLimit;
    private String ruleId;
    private Long minimalObservationCountInTimeWindow;

    public Long getMinimalObservationCountInTimeWindow() {
        return minimalObservationCountInTimeWindow;
    }

    public void setMinimalObservationCountInTimeWindow(Long minimalObservationCountInTimeWindow) {
        this.minimalObservationCountInTimeWindow = minimalObservationCountInTimeWindow;
    }

    public boolean isTimebased() {
        return getType() == ConditionType.TIME;
    }

    public boolean isStatistics() {
        return getType() == ConditionType.STATISTICS;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public Long getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(ConditionValue conditionValue) {
        if (getType() == null) {
            throw new IllegalStateException("Condition Type must be set before setting timeLimit");
        }
        if (isTimebased()) {
            this.timeLimit = conditionValue.getTimeLimit();
        } else if (isStatistics()) {
            this.timeLimit = conditionValue.getBaselineSecondsBack();
        }
    }

    public void setTimeLimit(long timeLimit) {
        this.timeLimit = timeLimit;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public Operators getOperator() {
        return operator;
    }

    public void setOperator(Operators operator) {
        this.operator = operator;
    }

    public ConditionType getType() {
        return type;
    }

    public void setType(ConditionType type) {
        this.type = type;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public DataType getComponentDataType() {
        return componentDataType;
    }

    public void setComponentDataType(DataType componentDataType) {
        this.componentDataType = componentDataType;
    }

    @Override
    @SuppressWarnings({"checkstyle:cyclomaticcomplexity"})
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final RuleCondition other = (RuleCondition) o;

        return Objects.equal(this.values, other.values) && Objects
                .equal(this.operator, other.operator) && Objects
                .equal(this.type, other.type) && Objects
                .equal(this.componentId, other.componentId) && Objects
                .equal(this.componentDataType, other.componentDataType) && Objects
                .equal(this.timeLimit, other.timeLimit) && Objects
                .equal(this.ruleId, other.ruleId) && Objects
                .equal(this.minimalObservationCountInTimeWindow, other.minimalObservationCountInTimeWindow);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(values, operator, type, componentDataType, componentId, timeLimit, ruleId, minimalObservationCountInTimeWindow);
    }
}
