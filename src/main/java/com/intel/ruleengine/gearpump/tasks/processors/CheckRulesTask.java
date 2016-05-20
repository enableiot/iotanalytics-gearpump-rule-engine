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

import com.intel.ruleengine.gearpump.data.RuleConditionsRepository;
import com.intel.ruleengine.gearpump.data.alerts.RuleConditionsHbaseRepository;
import com.intel.ruleengine.gearpump.rules.RulesChecker;
import com.intel.ruleengine.gearpump.tasks.InvalidMessageTypeException;
import com.intel.ruleengine.gearpump.tasks.RuleEngineTask;
import com.intel.ruleengine.gearpump.tasks.messages.RulesWithObservation;
import com.intel.ruleengine.gearpump.tasks.messages.controllers.MessageReceiver;
import io.gearpump.Message;
import io.gearpump.cluster.UserConfig;
import io.gearpump.streaming.javaapi.Processor;
import io.gearpump.streaming.task.TaskContext;

import java.util.List;


public class CheckRulesTask extends RuleEngineTask {

    private static final String TASK_NAME = "verifyAllRulesConditions";

    private final RuleConditionsRepository componentAlertsHbaseDao;

    public CheckRulesTask(TaskContext context, UserConfig userConf) {
        this(context, userConf, new RuleConditionsHbaseRepository(userConf));
    }

    public CheckRulesTask(TaskContext context, UserConfig userConf, RuleConditionsRepository componentAlertsHbaseDao) {
        super(context, userConf);
        this.componentAlertsHbaseDao = componentAlertsHbaseDao;
    }

    @Override
    public void onNext(Message message) {
        try {
            List<RulesWithObservation> rulesWithObservations = MessageReceiver.build(message).getMessage();

            RulesChecker rulesChecker = new RulesChecker(rulesWithObservations, componentAlertsHbaseDao);
            getMessageSender().send(rulesChecker.getCompletelyFulfilledRules());
        } catch (InvalidMessageTypeException ex) {
            getLogger().warn("Incorrect format of message found - {}", message.msg().getClass().getCanonicalName());
            getContext().output(new Message(message, now()));
        }
    }

    public static Processor getProcessor(UserConfig config, int parallelProcessorNumber) {
        return createProcessor(CheckRulesTask.class, config, parallelProcessorNumber, TASK_NAME);
    }

}
