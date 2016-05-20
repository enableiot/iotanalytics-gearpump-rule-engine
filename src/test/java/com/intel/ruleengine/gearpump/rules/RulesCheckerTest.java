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

import com.intel.ruleengine.gearpump.data.RuleConditionsRepository;
import com.intel.ruleengine.gearpump.data.alerts.ComponentObservation;
import com.intel.ruleengine.gearpump.data.alerts.ScanProperties;
import com.intel.ruleengine.gearpump.tasks.messages.Observation;
import com.intel.ruleengine.gearpump.tasks.messages.Rule;
import com.intel.ruleengine.gearpump.tasks.messages.RulesWithObservation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RulesCheckerTest {
    @Mock
    private RuleConditionsRepository ruleConditionsRepository;

    private Observation observation;

    private String componentId;

    private long observationTimestamp = System.currentTimeMillis();

    private final String conditionValue = "100.5";

    private List<ComponentObservation> fulfilledComponentObservations;

    private ComponentObservation fulfilledComponentObservation;

    private RulesChecker rulesChecker;

    @Before
    public void init() throws IOException {
        componentId = IdGenerator.generateId();
        observation = createObservation(conditionValue);
        fulfilledComponentObservation = new ComponentObservation(componentId, observationTimestamp);
        fulfilledComponentObservations = new ArrayList<>();
        fulfilledComponentObservations.add(fulfilledComponentObservation);
        mockAllConditionsToBeFulfilled();
    }

    private Observation createObservation(String value) {
        Observation observation = new Observation();
        observation.setCid(componentId);
        observation.setOn(observationTimestamp);
        observation.setValue(value);
        return observation;
    }


    private void mockAllConditionsToBeFulfilled() throws IOException {
        when(ruleConditionsRepository.getFulfilledConditionsForComponent(Mockito.<ScanProperties>any()))
                .thenReturn(fulfilledComponentObservations);
    }

    private void mockConditionsToBeNotFulfilled() throws IOException {
        when(ruleConditionsRepository.getFulfilledConditionsForComponent(Mockito.<ScanProperties>any()))
                .thenReturn(new ArrayList<ComponentObservation>());
    }

    @Test
    public void ruleWithORConditionsShouldBeAlwaysFulfilled() throws IOException {
        mockConditionsToBeNotFulfilled();
        Rule rule = RuleCreator.createRuleWithTwoRandomConditions(ConditionOperators.OR, Operators.EQUAL);
        rule.setFulfilled(true);
        rulesChecker = createRulesChecker(rule);
        List<RulesWithObservation> result = rulesChecker.getCompletelyFulfilledRules();
        assertRuleFulfilled(result);
    }

    @Test
    public void ruleWithANDConditionsShouldBeFulfilledIfAllConditionsAre() {
        Rule rule = RuleCreator.createRuleWithTwoRandomConditions(ConditionOperators.AND, Operators.EQUAL);
        rulesChecker = createRulesChecker(rule);
        rule.setFulfilled(true);
        List<RulesWithObservation> result = rulesChecker.getCompletelyFulfilledRules();
        assertRuleFulfilled(result);
    }

    @Test
    public void ruleWithANDConditionsShouldNotBeFulfilledIfConditionsAreNot() throws IOException {
        mockConditionsToBeNotFulfilled();
        Rule rule = RuleCreator.createRuleWithTwoRandomConditions(ConditionOperators.AND, Operators.EQUAL);
        rule.setFulfilled(false);
        rulesChecker = createRulesChecker(rule);
        List<RulesWithObservation> result = rulesChecker.getCompletelyFulfilledRules();
        assertNoRuleFulfilled(result);
    }

    private RulesChecker createRulesChecker(Rule rule) {
        return new RulesChecker(Arrays.asList(new RulesWithObservation(observation, Arrays.asList(rule))), ruleConditionsRepository);
    }

    private static void assertNoRuleFulfilled(List<RulesWithObservation> result) {
        assertTrue(result != null);
        assertTrue(result.size() == 0);
    }

    private static void assertRuleFulfilled(List<RulesWithObservation> result) {
        assertTrue(result != null);
        assertTrue(result.get(0).getRules() != null);
        assertTrue(result.get(0).getRules().size() == 1);
    }
}
