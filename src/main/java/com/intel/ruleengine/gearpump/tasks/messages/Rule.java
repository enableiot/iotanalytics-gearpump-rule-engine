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
import com.intel.ruleengine.gearpump.rules.ConditionOperators;
import com.intel.ruleengine.gearpump.rules.RuleStatus;

import java.util.List;
import java.util.stream.Collectors;

public class Rule {

    private String id;
    private String accountId;
    private boolean fulfilled;
    private ConditionOperators conditionOperator;
    private RuleStatus status;

    private List<RuleCondition> conditions;

    public boolean hasTimebasedCondition() {
        return conditions.stream().anyMatch(c -> c.isTimebased());
    }

    public boolean hasStatisticsCondition() {
        return conditions.stream().anyMatch(c -> c.isStatistics());
    }

    public boolean isFulfilled() {
        return fulfilled;
    }

    public void setFulfilled(boolean isFulfilled) {
        this.fulfilled = isFulfilled;
    }

    public ConditionOperators getConditionOperator() {
        return conditionOperator;
    }

    public void setConditionOperator(ConditionOperators conditionOperator) {
        this.conditionOperator = conditionOperator;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public List<RuleCondition> getConditions() {
        return conditions;
    }

    public List<RuleCondition> getConditionsForComponent(String componentId) {
        return conditions.stream()
                .filter(condition -> condition.getComponentId().equals(componentId))
                .collect(Collectors.toList());
    }

    public void setConditions(List<RuleCondition> conditions) {
        this.conditions = conditions;
    }

    public RuleStatus getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = RuleStatus.fromString(status);
    }

    public void setStatus(RuleStatus status) {
        this.status = status;
    }

    public boolean isActive() {
        return status != null && status.equals(RuleStatus.ACTIVE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Rule other = (Rule) o;

        return Objects.equal(this.fulfilled, other.fulfilled) && Objects
                .equal(this.id, other.id) && Objects
                .equal(this.accountId, other.accountId) && Objects
                .equal(this.conditionOperator, other.conditionOperator) && Objects
                .equal(this.conditions, other.conditions);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, accountId, fulfilled, conditionOperator, conditions);
    }
}
