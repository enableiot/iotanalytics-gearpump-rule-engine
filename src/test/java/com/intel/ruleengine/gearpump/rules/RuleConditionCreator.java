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

import com.intel.ruleengine.gearpump.tasks.messages.RuleCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RuleConditionCreator {

    public static final long TWO_MINUTES_TIME_WINDOW = 120;

    private final String ruleId;

    public RuleConditionCreator(String ruleId) {
        this.ruleId = ruleId;
    }

    public List<RuleCondition> createBasicConditions(Map<String, Map<Operators, List<String>>> componentsWithConditions) {
        List<RuleCondition> ruleConditions = new ArrayList<>();

        componentsWithConditions.keySet().stream()
                .forEach(componentId -> {
                    Map<Operators, List<String>> operatorsWithValues = componentsWithConditions.get(componentId);
                    operatorsWithValues.keySet().stream()
                            .forEach(operator ->
                                ruleConditions.add(createBasicRuleCondition(componentId, operator, operatorsWithValues.get(operator)))
                            );
                });

        return ruleConditions;
    }

    public List<RuleCondition> createTimeBasedConditions(Map<String, Map<Operators, List<String>>> componentsWithConditions) {
        List<RuleCondition> ruleConditions = new ArrayList<>();

        componentsWithConditions.keySet().stream()
                .forEach(componentId -> {
                    Map<Operators, List<String>> operatorsWithValues = componentsWithConditions.get(componentId);
                    operatorsWithValues.keySet().stream()
                            .forEach(operator ->
                                ruleConditions.add(createTimebasedRuleCondition(componentId, operator, operatorsWithValues.get(operator)))
                            );
                });

        return ruleConditions;
    }

    public List<RuleCondition> createStatisticsConditions(Map<String, Map<Operators, List<String>>> componentsWithConditions, long minimalObservationCount) {
        List<RuleCondition> ruleConditions = new ArrayList<>();

        componentsWithConditions.keySet().stream()
                .forEach(componentId -> {
                    Map<Operators, List<String>> operatorsWithValues = componentsWithConditions.get(componentId);
                    operatorsWithValues.keySet().stream()
                            .forEach(operator ->
                                ruleConditions.add(createStatisticRuleCondition(componentId, operator, operatorsWithValues.get(operator), minimalObservationCount))
                            );
                });

        return ruleConditions;
    }

    public RuleCondition createBasicRuleCondition(String componentId, Operators operator, List<String> values) {
        return createBasicRuleCondition(componentId, operator, values, DataType.NUMBER);
    }

    public RuleCondition createBasicRuleCondition(String componentId, Operators operator, List<String> values, DataType dataType) {
        RuleCondition ruleCondition = new RuleCondition();
        ruleCondition.setComponentId(componentId);
        ruleCondition.setComponentDataType(dataType);
        ruleCondition.setOperator(operator);
        ruleCondition.setType(ConditionType.BASIC);
        ruleCondition.setValues(values);
        ruleCondition.setRuleId(ruleId);
        return ruleCondition;
    }

    private RuleCondition createTimebasedRuleCondition(String componentId, Operators operator, List<String> values) {
        RuleCondition ruleCondition = createBasicRuleCondition(componentId, operator, values);
        ruleCondition.setType(ConditionType.TIME);
        ruleCondition.setTimeLimit(TWO_MINUTES_TIME_WINDOW);
        return ruleCondition;
    }

    private RuleCondition createStatisticRuleCondition(String componentId, Operators operator, List<String> values, long minimalObservationCount) {
        RuleCondition ruleCondition = createTimebasedRuleCondition(componentId, operator, values);
        ruleCondition.setType(ConditionType.STATISTICS);
        ruleCondition.setMinimalObservationCountInTimeWindow(minimalObservationCount);
        return ruleCondition;
    }
}
