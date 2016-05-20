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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.intel.ruleengine.gearpump.data.RulesRepository;
import com.intel.ruleengine.gearpump.tasks.TaskHelper;
import com.intel.ruleengine.gearpump.tasks.messages.Observation;
import com.intel.ruleengine.gearpump.tasks.messages.Rule;
import com.intel.ruleengine.gearpump.tasks.messages.RulesWithObservation;
import io.gearpump.Message;
import io.gearpump.streaming.task.TaskContext;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@PrepareForTest(TaskHelper.class)
@RunWith(PowerMockRunner.class)
public class GetComponentRulesTaskTest {
    @Mock
    private TaskContext taskContext;

    @Mock
    private RulesRepository rulesRepository;

    @Mock
    private Message message;

    private Gson gson = new Gson();

    private GetComponentRulesTask rulesTask;

    @Before
    public void init() {
        rulesTask = new GetComponentRulesTask(taskContext, null, rulesRepository);
        PowerMockito.mockStatic(TaskHelper.class);
        when(TaskHelper.now()).thenReturn(0L);
    }

    @Test
    public void onNextConditionsShouldBeFulfilled() throws IOException {
        Observation observation = new Observation();
        observation.setCid("cid");
        observation.setAid("aid");

        List<Rule> rules = Arrays.asList(new Rule());
        List<RulesWithObservation> rulesWithObservation = Arrays.asList(new RulesWithObservation(observation, rules));
        Message expectedOutput = new Message(gson.toJson(rulesWithObservation), 0L);

        when(message.msg()).thenReturn(Bytes.toBytes(gson.toJson(Arrays.asList(observation))));
        when(rulesRepository.getComponentsRules(anyString(), anySet())).thenReturn(ImmutableMap.of(observation.getCid(), rules));

        rulesTask.onNext(message);
        verify(taskContext, times(1)).output(expectedOutput);
    }

    @Test
    public void onNextShouldCatchInvalidMessageTypeException() throws IOException {
        when(message.msg()).thenReturn(new Observation());
        rulesTask.onNext(message);
        verify(taskContext, never()).output(any());
    }

    @Test
    public void onNextShouldCatchIOException() throws IOException {
        when(message.msg()).thenReturn(gson.toJson(new Observation()));
        when(rulesRepository.getComponentsRules(any(), any())).thenThrow(IOException.class);
        rulesTask.onNext(message);
        verify(taskContext, never()).output(any());
    }
}
