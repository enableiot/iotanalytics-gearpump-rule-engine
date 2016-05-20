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
import com.intel.ruleengine.gearpump.apiclients.InvalidDashboardResponseException;
import com.intel.ruleengine.gearpump.apiclients.rules.RulesApi;
import com.intel.ruleengine.gearpump.apiclients.rules.model.ComponentRulesResponse;
import com.intel.ruleengine.gearpump.apiclients.rules.model.ConditionComponent;
import com.intel.ruleengine.gearpump.apiclients.rules.model.ConditionValue;
import com.intel.ruleengine.gearpump.apiclients.rules.model.Conditions;
import com.intel.ruleengine.gearpump.apiclients.rules.model.RuleResponse;
import com.intel.ruleengine.gearpump.parsers.RuleParser;
import com.intel.ruleengine.gearpump.rules.RuleStatus;
import com.intel.ruleengine.gearpump.tasks.TaskHelper;
import com.intel.ruleengine.gearpump.tasks.messages.Rule;
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
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@PrepareForTest(TaskHelper.class)
@RunWith(PowerMockRunner.class)
public class DownloadRulesTaskTest {
    @Mock
    private TaskContext taskContext;

    @Mock
    private RulesApi rulesApi;

    @Mock
    private Message message;

    private Gson gson = new Gson();

    private DownloadRulesTask downloadRulesTask;

    @Before
    public void init() {
        downloadRulesTask = new DownloadRulesTask(taskContext, null, rulesApi);
        PowerMockito.mockStatic(TaskHelper.class);
        when(TaskHelper.now()).thenReturn(0L);
    }

    @Test
    public void onNextConditionsShouldBeFulfilled() throws InvalidDashboardResponseException {
        ConditionComponent conditionComponent = new ConditionComponent();
        conditionComponent.setDataType("STRING");

        ConditionValue conditionValue = new ConditionValue();
        conditionValue.setOperator("Equal");
        conditionValue.setType("BASIC");
        conditionValue.setComponent(conditionComponent);

        Conditions conditions = new Conditions();
        conditions.setOperator("NONE");
        conditions.setValues(Arrays.asList(conditionValue));

        RuleResponse ruleResponse = new RuleResponse();
        ruleResponse.setConditions(conditions);
        ruleResponse.setId("ruleResponse");
        ruleResponse.setStatus(RuleStatus.ACTIVE.toString());

        ComponentRulesResponse rulesResponse = new ComponentRulesResponse();
        rulesResponse.setComponentId("cid");
        rulesResponse.setRules(Arrays.asList(ruleResponse));

        List<ComponentRulesResponse> componentsRules = Arrays.asList(rulesResponse);

        Map<String, List<Rule>> expectedResult =  new RuleParser(componentsRules).getComponentRules();
        Message expectedOutput = new Message(gson.toJson(expectedResult), 0L);

        when(rulesApi.getActiveComponentsRules()).thenReturn(componentsRules);
        downloadRulesTask.onNext(message);

        verify(taskContext, times(1)).output(expectedOutput);
        verify(taskContext, times(1)).scheduleOnce(any(), any());
    }

    @Test
    public void onNextShouldCatchInvalidDashboardResponseException() throws InvalidDashboardResponseException {
        when(rulesApi.getActiveComponentsRules()).thenThrow(InvalidDashboardResponseException.class);
        downloadRulesTask.onNext(message);
        verify(taskContext, never()).output(any());
        verify(taskContext, times(1)).scheduleOnce(any(), any());
    }

    @Test
    public void onNextShouldCatchException() throws InvalidDashboardResponseException {
        when(rulesApi.getActiveComponentsRules()).thenThrow(Exception.class);
        downloadRulesTask.onNext(message);
        verify(taskContext, never()).output(any());
        verify(taskContext, times(1)).scheduleOnce(any(), any());
    }
}
