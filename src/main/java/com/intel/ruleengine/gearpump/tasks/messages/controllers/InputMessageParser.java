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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intel.ruleengine.gearpump.tasks.InvalidMessageTypeException;
import com.intel.ruleengine.gearpump.tasks.messages.Observation;
import com.intel.ruleengine.gearpump.tasks.messages.Rule;
import com.intel.ruleengine.gearpump.tasks.messages.RulesWithObservation;
import io.gearpump.Message;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;


public class InputMessageParser<ParsedObjectType> {

    private Gson gson;

    public InputMessageParser() {
        gson = new Gson();
    }

    public ParsedObjectType parseInputMapMessage(Message message) throws InvalidMessageTypeException {
        Object msg = message.msg();
        try {
            Type typeOfMessage = new TypeToken<Map<String, List<Rule>>>() { } .getType();
            return gson.fromJson((String) msg, typeOfMessage);
        } catch (ClassCastException ex) {
            throw new InvalidMessageTypeException(ex);
        }
    }

    public ParsedObjectType parseInputListMessage(String inputMessage) throws InvalidMessageTypeException {
        try {
            Type typeOfMessage = new TypeToken<List<Observation>>() { } .getType();
            return gson.fromJson(inputMessage, typeOfMessage);
        } catch (ClassCastException ex) {
            throw new InvalidMessageTypeException(ex);
        }
    }

    public ParsedObjectType parseInputListRulesMessage(Object inputMessage) throws InvalidMessageTypeException {
        try {
            Type typeOfMessage = new TypeToken<List<RulesWithObservation>>() { } .getType();
            return gson.fromJson((String) inputMessage, typeOfMessage);
        } catch (ClassCastException ex) {
            throw new InvalidMessageTypeException(ex);
        }
    }

}
