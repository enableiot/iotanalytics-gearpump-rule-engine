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
import com.intel.ruleengine.gearpump.tasks.messages.Observation;
import com.intel.ruleengine.gearpump.tasks.messages.RulesWithObservation;
import com.intel.ruleengine.gearpump.tasks.storage.RuleComponentsStorageManager;
import io.gearpump.Message;
import io.gearpump.streaming.task.TaskContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@PrepareForTest({RuleComponentsStorageManager.class, PersistComponentAlertsTask.class})
@RunWith(PowerMockRunner.class)
public class PersistComponentAlertsTaskTest {
    @Mock
    private TaskContext taskContext;

    @Mock
    private RuleConditionsRepository ruleConditionsRepository;

    @Mock
    private Message message;

    private Gson gson = new Gson();

    private PersistComponentAlertsTask persistComponentAlertsTask;

    RuleComponentsStorageManager componentsStorageManagerMock;

    @Before
    public void init() {
        persistComponentAlertsTask = new PersistComponentAlertsTask(taskContext, null, ruleConditionsRepository);
        componentsStorageManagerMock = Mockito.mock(RuleComponentsStorageManager.class);
    }

    @Test
    public void onStartConditionsShouldBeFulfilled() throws IOException {
        doThrow(IOException.class).when(ruleConditionsRepository).createTable();
        persistComponentAlertsTask.onStart(null);
        verify(ruleConditionsRepository, times(1)).createTable();
    }

    @Test
    public void onStartShouldCatchIOException() throws IOException {
        persistComponentAlertsTask.onStart(null);
        verify(ruleConditionsRepository, times(1)).createTable();
    }

    @Test
    public void onNextConditionsShouldBeFulfilled() throws Exception {
        List<RulesWithObservation> rulesWithObservation = Arrays.asList(new RulesWithObservation(new Observation(), new ArrayList<>()));

        PowerMockito.whenNew(RuleComponentsStorageManager.class)
                .withArguments(ruleConditionsRepository, rulesWithObservation)
                .thenReturn(componentsStorageManagerMock);
        when(message.msg()).thenReturn(gson.toJson(rulesWithObservation));

        persistComponentAlertsTask.onNext(message);

        verify(message, times(1)).msg();
        verify(componentsStorageManagerMock, times(1)).persistBasicAndStatisticsRuleComponents();
    }

    @Test
    public void onNextShouldCatchInvalidMessageTypeException() throws Exception {
        PowerMockito.whenNew(RuleComponentsStorageManager.class)
                .withAnyArguments()
                .thenReturn(componentsStorageManagerMock);

        when(message.msg()).thenReturn(new Observation());
        persistComponentAlertsTask.onNext(message);

        verify(message, times(2)).msg();
        verify(componentsStorageManagerMock, never()).persistBasicAndStatisticsRuleComponents();
    }

    @Test
    public void onNextShouldCatchIOException() throws Exception {
        List<RulesWithObservation> rulesWithObservation = Arrays.asList(new RulesWithObservation(new Observation(), new ArrayList<>()));

        PowerMockito.whenNew(RuleComponentsStorageManager.class)
                .withArguments(ruleConditionsRepository, rulesWithObservation)
                .thenReturn(componentsStorageManagerMock);
        when(message.msg()).thenReturn(gson.toJson(rulesWithObservation));
        doThrow(IOException.class).when(componentsStorageManagerMock).persistBasicAndStatisticsRuleComponents();

        persistComponentAlertsTask.onNext(message);

        verify(message, times(1)).msg();
        verify(componentsStorageManagerMock, times(1)).persistBasicAndStatisticsRuleComponents();
    }
}
