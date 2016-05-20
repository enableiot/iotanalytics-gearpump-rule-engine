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
import com.intel.ruleengine.gearpump.rules.ConditionOperators;
import com.intel.ruleengine.gearpump.tasks.TaskHelper;
import com.intel.ruleengine.gearpump.tasks.messages.Observation;
import com.intel.ruleengine.gearpump.tasks.messages.Rule;
import com.intel.ruleengine.gearpump.tasks.messages.RulesWithObservation;
import io.gearpump.Message;
import io.gearpump.streaming.task.TaskContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@PrepareForTest(TaskHelper.class)
@RunWith(PowerMockRunner.class)
public class CheckRulesTaskTest {
    @Mock
    private TaskContext taskContext;

    @Mock
    private RuleConditionsRepository ruleConditionsRepository;

    @Mock
    private Message message;

    private Gson gson = new Gson();

    private CheckRulesTask checkRulesTask;

    @Before
    public void init() {
        checkRulesTask = new CheckRulesTask(taskContext, null, ruleConditionsRepository);
        PowerMockito.mockStatic(TaskHelper.class);
        when(TaskHelper.now()).thenReturn(0L);
    }

    @Test
    public void onNextConditionsShouldBeFulfilled() {
        List<RulesWithObservation> rulesWithObservation = getRulesWithObservation(true);
        Message expectedOutput = new Message(gson.toJson(rulesWithObservation), 0L);

        when(message.msg()).thenReturn(gson.toJson(rulesWithObservation));
        checkRulesTask.onNext(message);

        verify(taskContext, times(1)).output(expectedOutput);
        verify(message, times(1)).msg();
    }

    @Test
    public void onNextConditionsShouldNotBeFulfilled() {
        when(message.msg()).thenReturn(gson.toJson(getRulesWithObservation(false)));
        checkRulesTask.onNext(message);

        verify(taskContext, never()).output(any());
        verify(message, times(1)).msg();
    }

    @Test
    public void onNextShouldCatchInvalidMessageTypeException() {
        when(message.msg()).thenReturn(new Observation());
        checkRulesTask.onNext(message);
        verify(taskContext, times(1)).output(new Message(message, 0L));
        verify(message, times(2)).msg();
    }

    private List<RulesWithObservation> getRulesWithObservation(boolean isRuleFulfilled) {
        Rule rule = new Rule();
        rule.setFulfilled(isRuleFulfilled);
        rule.setConditionOperator(ConditionOperators.NONE);

        Observation observation = new Observation();
        observation.setOn(System.currentTimeMillis());

        return Arrays.asList(new RulesWithObservation(observation, Arrays.asList(rule)));
    }
}
