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

import com.intel.ruleengine.gearpump.apiclients.DashboardConfigProvider;
import com.intel.ruleengine.gearpump.apiclients.alerts.AlertsApi;
import com.intel.ruleengine.gearpump.apiclients.alerts.DashboardAlertsApi;
import com.intel.ruleengine.gearpump.tasks.InvalidMessageTypeException;
import com.intel.ruleengine.gearpump.tasks.RuleEngineTask;
import com.intel.ruleengine.gearpump.tasks.messages.RulesWithObservation;
import com.intel.ruleengine.gearpump.tasks.messages.controllers.MessageReceiver;
import io.gearpump.Message;
import io.gearpump.cluster.UserConfig;
import io.gearpump.streaming.javaapi.Processor;
import io.gearpump.streaming.task.TaskContext;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"checkstyle:illegalcatch", "PMD.AvoidCatchingGenericException"})
public class SendAlertsTask extends RuleEngineTask {

    private static final String TASK_NAME = "sendAlerts";

    private List<RulesWithObservation> fulfilledRules;

    private final AlertsApi alertsApi;

    public SendAlertsTask(TaskContext context, UserConfig userConfig) {
        this(context, userConfig, new DashboardAlertsApi(new DashboardConfigProvider(userConfig)));
    }

    public SendAlertsTask(TaskContext context, UserConfig userConfig, AlertsApi alertsApi) {
        super(context, userConfig);
        this.alertsApi = alertsApi;
    }

    @Override
    public void onNext(Message message) {
        try {
            fulfilledRules = MessageReceiver.build(message).getMessage()
                    .stream().filter(r -> hasFulfilledRules(r))
                    .collect(Collectors.toList());

            sendAlerts();
        } catch (InvalidMessageTypeException ex) {
            getLogger().warn("Incorrect format of message found - {}", message.msg().getClass().getCanonicalName());
        }
        getContext().output(new Message(message, now()));
    }

    private void sendAlerts() {
        try {
            if (fulfilledRules != null && !fulfilledRules.isEmpty()) {
                alertsApi.pushAlert(fulfilledRules);
            }
        } catch (Exception e) {
            getLogger().error("Unable to send alerts for fulfilled rules", e);
        }
    }

    public static Processor getProcessor(UserConfig config, int parallelProcessorNumber) {
        return createProcessor(SendAlertsTask.class, config, parallelProcessorNumber, TASK_NAME);
    }
}
