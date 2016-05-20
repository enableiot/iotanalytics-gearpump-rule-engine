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

package com.intel.ruleengine.gearpump.tasks.processors;

import com.google.gson.Gson;
import com.intel.ruleengine.gearpump.data.RuleConditionsRepository;
import com.intel.ruleengine.gearpump.data.StatisticsRepository;
import com.intel.ruleengine.gearpump.rules.ConditionType;
import com.intel.ruleengine.gearpump.tasks.messages.Observation;
import com.intel.ruleengine.gearpump.tasks.messages.Rule;
import com.intel.ruleengine.gearpump.tasks.messages.RuleCondition;
import com.intel.ruleengine.gearpump.tasks.messages.RulesWithObservation;
import io.gearpump.Message;
import io.gearpump.streaming.task.TaskContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PersistObservationTaskTest {
    @Mock
    private TaskContext taskContext;

    @Mock
    private StatisticsRepository statisticsRepository;

    @Mock
    private RuleConditionsRepository ruleConditionsRepository;

    @Mock
    private Message message;

    private Gson gson = new Gson();

    private PersistObservationTask persistObservationTask;

    @Before
    public void init() {
        persistObservationTask = new PersistObservationTask(taskContext, null, statisticsRepository, ruleConditionsRepository);
    }

    @Test
    public void onStartConditionsShouldBeFulfilled() throws IOException {
        doThrow(IOException.class).when(statisticsRepository).createTable();
        persistObservationTask.onStart(null);
        verify(statisticsRepository, times(1)).createTable();
    }

    @Test
    public void onStartShouldCatchIOException() throws IOException {
        persistObservationTask.onStart(null);
        verify(statisticsRepository, times(1)).createTable();
    }

    @Test
    public void onNextConditionsShouldBeFulfilled() throws IOException {
        List<RulesWithObservation> rulesWithObservation = getRulesWithObservation(ConditionType.STATISTICS);
        List<Observation> expectedOutput = Arrays.asList(rulesWithObservation.get(0).getObservation());

        when(message.msg()).thenReturn(gson.toJson(rulesWithObservation));
        persistObservationTask.onNext(message);

        verify(message, times(1)).msg();
        verify(statisticsRepository, times(1)).putObservationForStatisticsRuleCondition(expectedOutput);
    }

    @Test
    public void onNextConditionsShouldNotBeFulfilled() throws IOException {
        when(message.msg()).thenReturn(gson.toJson(getRulesWithObservation(ConditionType.BASIC)));
        persistObservationTask.onNext(message);

        verify(message, times(1)).msg();
        verify(statisticsRepository, never()).putObservationForStatisticsRuleCondition(any());
    }

    @Test
    public void onNextShouldCatchInvalidMessageTypeException() throws IOException {
        when(message.msg()).thenReturn(new Observation());
        persistObservationTask.onNext(message);

        verify(message, times(2)).msg();
        verify(statisticsRepository, never()).putObservationForStatisticsRuleCondition(any());
    }

    @Test
    public void onNextShouldCatchIOException() throws IOException {
        List<RulesWithObservation> rulesWithObservation = getRulesWithObservation(ConditionType.STATISTICS);
        List<Observation> expectedOutput = Arrays.asList(rulesWithObservation.get(0).getObservation());

        when(message.msg()).thenReturn(gson.toJson(rulesWithObservation));
        doThrow(IOException.class).when(statisticsRepository).putObservationForStatisticsRuleCondition(any());
        persistObservationTask.onNext(message);

        verify(message, times(1)).msg();
        verify(statisticsRepository, times(1)).putObservationForStatisticsRuleCondition(expectedOutput);
    }

    private List<RulesWithObservation> getRulesWithObservation(ConditionType conditionType) {
        RuleCondition ruleCondition = new RuleCondition();
        ruleCondition.setType(conditionType);

        Rule rule = new Rule();
        rule.setConditions(Arrays.asList(ruleCondition));

        return Arrays.asList(new RulesWithObservation(new Observation(), Arrays.asList(rule)));
    }
}
