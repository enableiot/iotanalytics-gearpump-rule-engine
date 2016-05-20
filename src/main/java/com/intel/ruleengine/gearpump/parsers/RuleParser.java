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

package com.intel.ruleengine.gearpump.parsers;

import com.intel.ruleengine.gearpump.apiclients.rules.model.ComponentRulesResponse;
import com.intel.ruleengine.gearpump.apiclients.rules.model.ConditionValue;
import com.intel.ruleengine.gearpump.apiclients.rules.model.Conditions;
import com.intel.ruleengine.gearpump.apiclients.rules.model.RuleResponse;
import com.intel.ruleengine.gearpump.rules.ConditionOperators;
import com.intel.ruleengine.gearpump.rules.ConditionType;
import com.intel.ruleengine.gearpump.rules.DataType;
import com.intel.ruleengine.gearpump.rules.Operators;
import com.intel.ruleengine.gearpump.tasks.messages.Rule;
import com.intel.ruleengine.gearpump.tasks.messages.RuleCondition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RuleParser {

    private final List<ComponentRulesResponse> componentsRulesResponse;

    public RuleParser(List<ComponentRulesResponse> componentsRulesResponse) {
        this.componentsRulesResponse = componentsRulesResponse;
    }

    public Map<String, List<Rule>> getComponentRules() {
        Map<String, List<Rule>> result = new HashMap<>();

        componentsRulesResponse.forEach(componentRules -> {
            result.put(componentRules.getComponentId(), getRules(componentRules));

        });
        return result;
    }

    public List<Rule> getRules(ComponentRulesResponse componentRules) {
        return componentRules.getRules().stream()
                .map(rule -> toRule(rule))
                .collect(Collectors.toList());
    }

    private Rule toRule(RuleResponse response) {
        Rule rule = new Rule();
        rule.setAccountId(response.getDomainId());
        rule.setId(response.getId());
        rule.setConditions(toRuleConditions(rule.getId(), response.getConditions()));
        rule.setConditionOperator(ConditionOperators.fromString(response.getConditions().getOperator()));
        rule.setStatus(response.getStatus());
        return rule;
    }

    private List<RuleCondition> toRuleConditions(String ruleId, Conditions responseConditions) {
        return responseConditions.getValues().stream()
                .map(condition -> toRuleCondition(ruleId, condition))
                .collect(Collectors.toList());
    }

    private RuleCondition toRuleCondition(String ruleId, ConditionValue condition) {
        RuleCondition ruleCondition = new RuleCondition();
        ruleCondition.setComponentId(condition.getComponent().getCid());
        ruleCondition.setComponentDataType(DataType.valueOf(condition.getComponent().getDataType().toUpperCase()));
        ruleCondition.setOperator(Operators.fromString(condition.getOperator()));
        ruleCondition.setType(ConditionType.valueOf(condition.getType().toUpperCase()));
        ruleCondition.setValues(condition.getValues());
        ruleCondition.setTimeLimit(condition);
        ruleCondition.setRuleId(ruleId);
        ruleCondition.setMinimalObservationCountInTimeWindow(condition.getBaselineMinimalInstances());

        return ruleCondition;
    }
}
