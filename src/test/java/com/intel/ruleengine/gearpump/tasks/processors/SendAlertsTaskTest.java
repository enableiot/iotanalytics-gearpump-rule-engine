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
import com.intel.ruleengine.gearpump.apiclients.alerts.AlertsApi;
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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@PrepareForTest(TaskHelper.class)
@RunWith(PowerMockRunner.class)
public class SendAlertsTaskTest {
    @Mock
    private TaskContext taskContext;

    @Mock
    private AlertsApi alertsApi;

    @Mock
    private Message message;

    private Gson gson = new Gson();

    private SendAlertsTask sendAlertsTask;

    @Before
    public void init() {
        sendAlertsTask = new SendAlertsTask(taskContext, null, alertsApi);
        PowerMockito.mockStatic(TaskHelper.class);
        when(TaskHelper.now()).thenReturn(0L);
    }

    @Test
    public void OnNextConditionsShouldBeFulfilled() throws InvalidDashboardResponseException {
        Message expectedOutput = new Message(message, 0L);

        when(message.msg()).thenReturn(gson.toJson(
                Arrays.asList(new RulesWithObservation(new Observation(), Arrays.asList(new Rule()))))
        );
        sendAlertsTask.onNext(message);

        verify(message, times(1)).msg();
        verify(alertsApi, times(1)).pushAlert(any());
        verify(taskContext, times(1)).output(expectedOutput);
    }

    @Test
    public void OnNextConditionsShouldNotBeFulfilled() throws InvalidDashboardResponseException {
        Message expectedOutput = new Message(message, 0L);

        when(message.msg()).thenReturn(gson.toJson(
                Arrays.asList(new RulesWithObservation(new Observation(), null)))
        );
        sendAlertsTask.onNext(message);

        verify(message, times(1)).msg();
        verify(alertsApi, never()).pushAlert(any());
        verify(taskContext, times(1)).output(expectedOutput);
    }

    @Test
    public void OnNextShouldCatchInvalidDashboardResponseException() throws InvalidDashboardResponseException {
        Message expectedOutput = new Message(message, 0L);

        when(message.msg()).thenReturn(new Observation());
        sendAlertsTask.onNext(message);

        verify(message, times(2)).msg();
        verify(alertsApi, never()).pushAlert(any());
        verify(taskContext, times(1)).output(expectedOutput);
    }

    @Test
    public void OnNextShouldCatchException() throws InvalidDashboardResponseException {
        Message expectedOutput = new Message(message, 0L);

        when(message.msg()).thenReturn(gson.toJson(
                Arrays.asList(new RulesWithObservation(new Observation(), Arrays.asList(new Rule()))))
        );
        doThrow(Exception.class).when(alertsApi).pushAlert(any());
        sendAlertsTask.onNext(message);

        verify(message, times(1)).msg();
        verify(alertsApi, times(1)).pushAlert(any());
        verify(taskContext, times(1)).output(expectedOutput);
    }
}
