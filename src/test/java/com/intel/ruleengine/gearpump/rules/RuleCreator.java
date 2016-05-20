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

import com.google.common.collect.ImmutableMap;
import com.intel.ruleengine.gearpump.tasks.messages.Rule;
import org.apache.commons.lang.RandomStringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RuleCreator {

    private Rule rule;
    private RuleConditionCreator ruleConditionCreator;
    private final ConditionOperators ruleConditionsOperator;

    public RuleCreator(ConditionOperators ruleConditionsOperator) {
        this.ruleConditionsOperator = ruleConditionsOperator;
    }

    public Rule createBasicRule(Map<String, Map<Operators, List<String>>> componentsWithConditions) {
        buildRule();
        rule.setConditions(ruleConditionCreator.createBasicConditions(componentsWithConditions));
        return rule;
    }

    public Rule createTimebasedRule(Map<String, Map<Operators, List<String>>> componentsWithConditions) {
        buildRule();
        rule.setConditions(ruleConditionCreator.createTimeBasedConditions(componentsWithConditions));
        return rule;
    }

    public Rule createStatisticsRule(Map<String, Map<Operators, List<String>>> componentsWithConditions, long minimalObservationCount) {
        buildRule();
        rule.setConditions(ruleConditionCreator.createStatisticsConditions(componentsWithConditions, minimalObservationCount));
        return rule;
    }

    public static Rule copyRule(Rule old) {
        Rule newRule = new Rule();
        newRule.setId(old.getId());
        newRule.setAccountId(old.getAccountId());
        newRule.setConditionOperator(old.getConditionOperator());
        newRule.setStatus(old.getStatus());
        newRule.setConditions(old.getConditions());
        newRule.setConditionOperator(old.getConditionOperator());
        newRule.setFulfilled(old.isFulfilled());
        return newRule;
    }

    private void buildRule() {
        rule = new Rule();
        setIds();
        rule.setConditionOperator(ruleConditionsOperator);
        rule.setStatus(RuleStatus.ACTIVE);
        ruleConditionCreator = new RuleConditionCreator(rule.getId());
    }

    private void setIds() {
        rule.setId(IdGenerator.generateId());
        rule.setAccountId(IdGenerator.generateId());
    }

    public static Rule createRuleWithSingleCondition(Operators conditionOperator, String conditionValue, String componentId) {
        Map conditions = ImmutableMap.of(conditionOperator, Arrays.asList(conditionValue));
        return new RuleCreator(ConditionOperators.NONE).createBasicRule(ImmutableMap.of(componentId, conditions));
    }

    public static Rule createRuleWithSingleTimeBasedCondition(Operators conditionOperator, String conditionValue, String componentId) {
        Map conditions = ImmutableMap.of(conditionOperator, Arrays.asList(conditionValue));
        return new RuleCreator(ConditionOperators.NONE).createTimebasedRule(ImmutableMap.of(componentId, conditions));
    }

    public static Rule createRuleWithStatisticsStdDevCondition(Operators conditionOperator, String conditionValue, String componentId, long minimalObservationCount) {
        Map conditions = ImmutableMap.of(conditionOperator, Arrays.asList(conditionValue));
        return new RuleCreator(ConditionOperators.NONE).createStatisticsRule(ImmutableMap.of(componentId, conditions), minimalObservationCount);
    }

    public static Rule createRuleWithTwoConditionsForComponentWithAND(String conditionValue, String componentId) {
        return createRuleWithTwoConditionsEqualAndGreater(conditionValue, componentId, ConditionOperators.AND);
    }

    public static Rule createRuleWithTwoConditionsForComponentWithOR(String conditionValue, String componentId) {
        return createRuleWithTwoConditionsEqualAndGreater(conditionValue, componentId, ConditionOperators.OR);
    }

    private static Rule createRuleWithTwoConditionsEqualAndGreater(String conditionValue, String componentId, ConditionOperators ruleConditionsOperator) {
        Map conditions = ImmutableMap.of(Operators.EQUAL, Arrays.asList(conditionValue), Operators.GREATER_EQ, Arrays.asList(conditionValue));
        return new RuleCreator(ruleConditionsOperator).createBasicRule(ImmutableMap.of(componentId, conditions));
    }

    public static Rule createRuleWithTwoRandomConditions(ConditionOperators ruleConditionsOperator, Operators conditionsOperator) {
        String conditionValue = RandomStringUtils.randomNumeric(2);
        Map conditions = ImmutableMap.of(conditionsOperator, Arrays.asList(conditionValue));
        return new RuleCreator(ruleConditionsOperator).createBasicRule
                (ImmutableMap.of(IdGenerator.generateId(), conditions,
                        IdGenerator.generateId(), conditions));
    }
}
