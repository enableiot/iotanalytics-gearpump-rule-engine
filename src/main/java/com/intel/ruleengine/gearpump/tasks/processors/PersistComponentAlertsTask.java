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
import com.intel.ruleengine.gearpump.tasks.InvalidMessageTypeException;
import com.intel.ruleengine.gearpump.tasks.RuleEngineTask;
import com.intel.ruleengine.gearpump.tasks.messages.RulesWithObservation;
import com.intel.ruleengine.gearpump.tasks.messages.controllers.MessageReceiver;
import com.intel.ruleengine.gearpump.tasks.storage.RuleComponentsStorageManager;
import io.gearpump.Message;
import io.gearpump.cluster.UserConfig;
import io.gearpump.streaming.javaapi.Processor;
import io.gearpump.streaming.task.StartTime;
import io.gearpump.streaming.task.TaskContext;

import java.io.IOException;
import java.util.List;

public class PersistComponentAlertsTask extends RuleEngineTask {

    private static final String TASK_NAME = "persistComponentAlerts";

    private RuleConditionsRepository ruleConditionsRepository;

    public PersistComponentAlertsTask(TaskContext taskContext, UserConfig userConf) {
        this(taskContext, userConf, new RuleConditionsHbaseRepository(userConf));
    }

    public PersistComponentAlertsTask(TaskContext taskContext, UserConfig userConf, RuleConditionsRepository ruleConditionsRepository) {
        super(taskContext, userConf);
        this.ruleConditionsRepository = ruleConditionsRepository;

    }

    @Override
    public void onStart(StartTime startTime) {
        try {
            ruleConditionsRepository.createTable();
        } catch (IOException ex) {
            getLogger().warn("Initialization of hbase failed");
        }
    }

    @Override
    public void onNext(Message message) {
        try {
            List<RulesWithObservation> checkedRulesWithObservation = MessageReceiver.build(message).getMessage();

            RuleComponentsStorageManager ruleComponentsStorageManager = new RuleComponentsStorageManager(ruleConditionsRepository, checkedRulesWithObservation);
            ruleComponentsStorageManager.persistBasicAndStatisticsRuleComponents();

            getLogger().info("Sendin message to check rullesss");
            getMessageSender().send(checkedRulesWithObservation);

        } catch (InvalidMessageTypeException e) {
            getLogger().warn("Incorrect format of message found - {}", message.msg().getClass().getCanonicalName());
        } catch (IOException e) {
            getLogger().error("Error during persisting rules in hbase.", e);
        }
    }

    public static Processor getProcessor(UserConfig config, int parallelProcessorNumber) {
        return createProcessor(PersistComponentAlertsTask.class, config, parallelProcessorNumber, TASK_NAME);
    }
}
