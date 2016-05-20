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
package com.intel.ruleengine.gearpump.tasks.messages.controllers;

import com.intel.ruleengine.gearpump.tasks.messages.RulesWithObservation;
import io.gearpump.Message;
import io.gearpump.streaming.task.TaskContext;

import java.util.List;

public class MessageSender {

    private final TaskContext context;
    private List<RulesWithObservation> rulesWithObservations;
    private static final int MAX_OBSERVATION_NUMBER = 1024;
    private static final int MAX_RULES_OBSERVATIONS_PER_MESSAGE = MAX_OBSERVATION_NUMBER / 8;

    public MessageSender(TaskContext context) {
        this.context = context;
    }

    public void send(List<RulesWithObservation> rulesWithObservation) {
        this.rulesWithObservations = rulesWithObservation;
        sendInParts();
    }

    private void sendInParts() {
        for (int i = 0; i < rulesWithObservations.size(); i += MAX_RULES_OBSERVATIONS_PER_MESSAGE) {
            context.output(getOutputMessage(rulesWithObservations.subList(i, getEndIndex(i))));
        }
    }

    private int getEndIndex(int startIndex) {
        return Math.min(startIndex + MAX_RULES_OBSERVATIONS_PER_MESSAGE, rulesWithObservations.size());
    }

    private Message getOutputMessage(List<RulesWithObservation> rulesWithObservations) {
        return new OutputMessageCreator<List<RulesWithObservation>>().createOutputMessage(rulesWithObservations);
    }
}
