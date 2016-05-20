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

import com.intel.ruleengine.gearpump.tasks.InvalidMessageTypeException;
import com.intel.ruleengine.gearpump.tasks.messages.RulesWithObservation;
import io.gearpump.Message;

import java.util.List;

public final class MessageReceiver {

    private final Message message;

    private MessageReceiver(Message message) {
        this.message = message;
    }

    public static MessageReceiver build(Message message) {
        return new MessageReceiver(message);
    }

    public List<RulesWithObservation> getMessage() throws InvalidMessageTypeException {
        return new InputMessageParser<List<RulesWithObservation>>().parseInputListRulesMessage(message.msg());
    }
}
