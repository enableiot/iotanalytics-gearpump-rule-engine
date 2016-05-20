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
import com.intel.ruleengine.gearpump.apiclients.rules.RulesApi;
import com.intel.ruleengine.gearpump.data.RulesRepository;
import com.intel.ruleengine.gearpump.tasks.messages.Observation;
import com.intel.ruleengine.gearpump.tasks.messages.Rule;
import io.gearpump.Message;
import io.gearpump.streaming.task.TaskContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PersistRulesTaskTest {
    @Mock
    private TaskContext taskContext;

    @Mock
    private RulesRepository rulesRepository;

    @Mock
    private RulesApi rulesApi;

    @Mock
    private Message message;

    private Gson gson = new Gson();

    private PersistRulesTask persistRulesTask;

    @Before
    public void init() {
        persistRulesTask = new PersistRulesTask(taskContext, null, rulesRepository, rulesApi);
    }

    @Test
    public void onStartConditionsShouldBeFulfilled() throws IOException {
        doThrow(IOException.class).when(rulesRepository).createTable();
        persistRulesTask.onStart(null);
        verify(rulesRepository, times(1)).createTable();
    }

    @Test
    public void onStartShouldCatchIOException() throws IOException {
        persistRulesTask.onStart(null);
        verify(rulesRepository, times(1)).createTable();
    }

    @Test
    public void onNextConditionsShouldBeFulfilled() throws IOException {
        Map<String, List<Rule>> ruleMap = getRulesMap(true);

        when(message.msg()).thenReturn(gson.toJson(ruleMap));
        persistRulesTask.onNext(message);

        verify(message, times(1)).msg();
        verify(rulesRepository, times(1)).putRulesAndRemoveNotExistingOnes(ruleMap);
    }

    @Test
    public void onNextConditionsShouldNotBeFulfilled() throws IOException {
        when(message.msg()).thenReturn(gson.toJson(getRulesMap(false)));
        persistRulesTask.onNext(message);

        verify(message, times(1)).msg();
        verify(rulesRepository, never()).putRulesAndRemoveNotExistingOnes(any());
    }

    @Test
    public void onNextShouldCatchInvalidMessageTypeException() throws IOException {
        when(message.msg()).thenReturn(new Observation());
        persistRulesTask.onNext(message);

        verify(message, times(1)).msg();
        verify(rulesRepository, never()).putRulesAndRemoveNotExistingOnes(any());
    }

    @Test
    public void onNextShouldCatchIOException() throws IOException {
        Map<String, List<Rule>> ruleMap = getRulesMap(true);

        when(message.msg()).thenReturn(gson.toJson(ruleMap));
        doThrow(IOException.class).when(rulesRepository).putRulesAndRemoveNotExistingOnes(anyMap());
        persistRulesTask.onNext(message);

        verify(message, times(1)).msg();
        verify(rulesRepository, times(1)).putRulesAndRemoveNotExistingOnes(ruleMap);
    }

    private Map<String, List<Rule>> getRulesMap(boolean notEmptyRulesList) {
        Map<String, List<Rule>> rules = new HashMap<>();
        if (notEmptyRulesList) {
            rules.put("key", Arrays.asList(new Rule()));
        } else {
            rules.put("key", null);
        }
        return rules;
    }
}
