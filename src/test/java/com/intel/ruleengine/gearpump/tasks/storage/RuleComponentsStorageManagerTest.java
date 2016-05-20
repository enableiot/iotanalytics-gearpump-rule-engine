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

package com.intel.ruleengine.gearpump.tasks.storage;

import com.google.common.collect.ImmutableList;
import com.intel.ruleengine.gearpump.data.RuleConditionsRepository;
import com.intel.ruleengine.gearpump.rules.Operators;
import com.intel.ruleengine.gearpump.rules.RuleCreator;
import com.intel.ruleengine.gearpump.tasks.messages.Observation;
import com.intel.ruleengine.gearpump.tasks.messages.Rule;
import com.intel.ruleengine.gearpump.tasks.messages.RulesWithObservation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RuleComponentsStorageManager.class, TimebasedRuleComponentsStorage.class, BasicRuleComponentsStorage.class})
public class RuleComponentsStorageManagerTest {
    @Mock
    private RuleConditionsRepository ruleConditionsRepository;

    private RuleComponentsStorageManager ruleComponentsStorageManager;
    private TimebasedRuleComponentsStorage timebasedRuleComponentsStorage;
    private BasicRuleComponentsStorage basicRuleComponentsStorage;

    private List<RulesWithObservation> basicStatisticsRulesWithObservation;
    private List<RulesWithObservation> timebasedRulesWithObservation;

    @Before
    public void init() {
        timebasedRuleComponentsStorage = Mockito.mock(TimebasedRuleComponentsStorage.class);
        basicRuleComponentsStorage = Mockito.mock(BasicRuleComponentsStorage.class);
    }

    @Test
    public void persistConditionsShouldBeFulfilled() throws Exception {
        RulesWithObservation rulesWithObservation = getRulesWithObservation();
        ruleComponentsStorageManager = new RuleComponentsStorageManager(ruleConditionsRepository, ImmutableList.of(rulesWithObservation));

        PowerMockito.whenNew(BasicRuleComponentsStorage.class)
                .withArguments(ruleConditionsRepository, basicStatisticsRulesWithObservation)
                .thenReturn(basicRuleComponentsStorage);
        PowerMockito.whenNew(TimebasedRuleComponentsStorage.class)
                .withArguments(ruleConditionsRepository, timebasedRulesWithObservation)
                .thenReturn(timebasedRuleComponentsStorage);

        ruleComponentsStorageManager.persistBasicAndStatisticsRuleComponents();
        ruleComponentsStorageManager.persistTimebasedRuleComponents();

        verify(timebasedRuleComponentsStorage, times(1)).persist();
        verify(basicRuleComponentsStorage, times(1)).persist();
    }

    private RulesWithObservation getRulesWithObservation() {
        Rule basicRule = RuleCreator.createRuleWithSingleCondition(Operators.LIKE, "val", "cid");
        Rule timebaseRule = RuleCreator.createRuleWithSingleTimeBasedCondition(Operators.LIKE, "val", "cid");

        Observation observation = new Observation();
        observation.setCid("cid");
        observation.setValue("val");

        basicStatisticsRulesWithObservation = Arrays.asList(new RulesWithObservation(observation, Arrays.asList(basicRule)));
        timebasedRulesWithObservation = Arrays.asList(new RulesWithObservation(observation, Arrays.asList(timebaseRule)));

        return new RulesWithObservation(observation, Arrays.asList(basicRule, timebaseRule));
    }
}
