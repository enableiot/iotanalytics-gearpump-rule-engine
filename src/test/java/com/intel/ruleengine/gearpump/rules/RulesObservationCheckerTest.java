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
import com.intel.ruleengine.gearpump.data.StatisticsRepository;
import com.intel.ruleengine.gearpump.data.alerts.ComponentObservation;
import com.intel.ruleengine.gearpump.data.alerts.ScanProperties;
import com.intel.ruleengine.gearpump.data.statistics.StatisticsValues;
import com.intel.ruleengine.gearpump.rules.conditions.StatisticsConditionChecker;
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RulesObservationCheckerTest {

    private static final String SECOND_STD_DEV_VALUE = "2";
    private static final String THIRD_STD_DEV_VALUE = "3";

    @Mock
    private RuleConditionsRepository ruleConditionsRepository;
    @Mock
    private StatisticsRepository statisticsRepository;

    private Observation observation;

    private RulesObservationChecker rulesObservationChecker;

    private String componentId;

    private long observationTimestamp = System.currentTimeMillis();

    private String conditionValue;

    private String observationValue;

    private List<ComponentObservation> fulfilledComponentObservations;

    private ComponentObservation fulfilledComponentObservation;
    private final double observationCountInTimeWindowResult = 5.0;
    private StatisticsValues statisticsValues;
    private final double averageResult = 10.0;
    private final double standardDeviationResult = 20.0;

    @Before
    public void init() throws IOException {
        componentId = IdGenerator.generateId();
        fulfilledComponentObservation = new ComponentObservation(componentId, observationTimestamp);
        fulfilledComponentObservations = new ArrayList<>();
        fulfilledComponentObservations.add(fulfilledComponentObservation);
        statisticsValues = new StatisticsValues(averageResult, standardDeviationResult);
        conditionValue = "100.5";
        setObservationToFulfillEqualCondition();
        prepareMocks();
    }

    private Observation createObservation(String value) {
        Observation observation = new Observation();
        observation.setCid(componentId);
        observation.setOn(observationTimestamp);
        observation.setValue(value);
        return observation;
    }

    private void setObservationToFulfillEqualCondition() {
        observationValue = conditionValue;
        observation = createObservation(observationValue);
    }

    private void setObservationToNotFulfillEqualCondition() {
        observationValue = conditionValue + 1;
        observation = createObservation(observationValue);
    }

    private void prepareMocks() throws IOException {
        when(ruleConditionsRepository.getFulfilledConditionsForComponent(Mockito.<ScanProperties>any()))
                .thenReturn(fulfilledComponentObservations);
        when(ruleConditionsRepository.getLastTimebasedComponentObservation(Mockito.<ScanProperties>any(), Mockito.eq(true)))
                .thenReturn(fulfilledComponentObservation);
        when(ruleConditionsRepository.getLastTimebasedComponentObservation(Mockito.<ScanProperties>any(), Mockito.eq(false)))
                .thenReturn(null);
        when(statisticsRepository.getObservationCount(Mockito.<ScanProperties>any()))
                .thenReturn(observationCountInTimeWindowResult);
        when(statisticsRepository.getStatisticsValuesForObservation(Mockito.<ScanProperties>any()))
                .thenReturn(statisticsValues);
    }

    @Test
    public void ruleEqualConditionShouldBeFulfilledIfObservationValueMetCondition() {
        setObservationToFulfillEqualCondition();

        List<Rule> inputRules = Arrays.asList(RuleCreator.createRuleWithSingleCondition(Operators.EQUAL, conditionValue, componentId));
        RulesWithObservation result = executeCheckRules(inputRules);

        assertOneRuleFulfilled(result);
    }

    @Test
    public void ruleTimebasedEqualConditionShouldBeFulfilledIfObservationValueMetCondition() {
        List<Rule> inputRules = Arrays.asList(
                RuleCreator.createRuleWithSingleTimeBasedCondition(Operators.EQUAL, conditionValue, componentId));
        RulesWithObservation result = executeCheckRules(inputRules);

        assertOneRuleFulfilled(result);
    }

    @Test
    public void ruleTimebasedEqualConditionShouldBeNotFulfilledIfObservationValueMetCondition() {
        List<Rule> inputRules = Arrays.asList(
                RuleCreator.createRuleWithSingleTimeBasedCondition(Operators.EQUAL, conditionValue, componentId));
        setObservationToNotFulfillEqualCondition();

        RulesWithObservation result = executeCheckRules(inputRules);

        assertNoRuleFulfilled(result);
    }

    @Test
    public void ruleStatistics2StdDevConditionShouldBeFulfilledIfObservationValueMetCondition() {
        long minimalObservationCount = (long) observationCountInTimeWindowResult;
        String valueForFulfillGrater2StdDevCondition =
                StatisticsConditionChecker.calculateStatisticsConditionValue(Double.valueOf(SECOND_STD_DEV_VALUE), averageResult, standardDeviationResult) + 1;
        observation.setValue(valueForFulfillGrater2StdDevCondition);
        List<Rule> inputRules = Arrays.asList(
                RuleCreator.createRuleWithStatisticsStdDevCondition(Operators.GREATER, SECOND_STD_DEV_VALUE, componentId, minimalObservationCount));

        RulesWithObservation result = executeCheckRules(inputRules);

        assertOneRuleFulfilled(result);
    }

    @Test
    public void ruleStatistics2StdDevConditionShouldNotBeFulfilledIfTheAreNpEnoughObservations() {
        long minimalFailingObservationCount = (long) observationCountInTimeWindowResult + 1;
        List<Rule> inputRules = Arrays.asList(
                RuleCreator.createRuleWithStatisticsStdDevCondition(Operators.GREATER, SECOND_STD_DEV_VALUE, componentId, minimalFailingObservationCount));

        RulesWithObservation result = executeCheckRules(inputRules);

        assertNoRuleFulfilled(result);
    }

    @Test
    public void ruleTimebasedEqualConditionShouldNotFulfilledIfThereAreNoObservationsInTimeWindow() throws IOException {
        when(ruleConditionsRepository.getLastTimebasedComponentObservation(Mockito.<ScanProperties>any(), Mockito.anyBoolean()))
                .thenReturn(null);
        List<Rule> inputRules = Arrays.asList(
                RuleCreator.createRuleWithSingleTimeBasedCondition(Operators.EQUAL, conditionValue, componentId));

        RulesWithObservation result = executeCheckRules(inputRules);

        assertNoRuleFulfilled(result);
    }

    private RulesWithObservation executeCheckRules(List<Rule> inputRules) {
        RulesWithObservation inputRulesWithObservation = new RulesWithObservation(observation, inputRules);
        rulesObservationChecker = new RulesObservationChecker(inputRulesWithObservation, ruleConditionsRepository, statisticsRepository);
        return rulesObservationChecker.checkRulesForObservation();
    }

    public static void assertOneRuleFulfilled(RulesWithObservation result) {
        assertTrue(result != null);
        assertTrue(result.getRules() != null);
        assertTrue(result.getRules().size() == 1);
        assertTrue(result.getRules().get(0).isFulfilled());
    }

    public static void assertNoRuleFulfilled(RulesWithObservation result) {
        assertTrue(result != null);
        assertTrue(result.getRules() != null);
        assertFalse(result.getRules().get(0).isFulfilled());
    }

}
