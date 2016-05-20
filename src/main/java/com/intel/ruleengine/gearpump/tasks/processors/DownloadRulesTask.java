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
import com.intel.ruleengine.gearpump.apiclients.DashboardConfigProvider;
import com.intel.ruleengine.gearpump.apiclients.InvalidDashboardResponseException;
import com.intel.ruleengine.gearpump.apiclients.rules.DashboardRulesApi;
import com.intel.ruleengine.gearpump.apiclients.rules.RulesApi;
import com.intel.ruleengine.gearpump.apiclients.rules.model.ComponentRulesResponse;
import com.intel.ruleengine.gearpump.parsers.RuleParser;
import com.intel.ruleengine.gearpump.tasks.RuleEngineTask;
import com.intel.ruleengine.gearpump.tasks.messages.Rule;
import com.intel.ruleengine.gearpump.tasks.messages.controllers.OutputMessageCreator;
import io.gearpump.Message;
import io.gearpump.cluster.UserConfig;
import io.gearpump.streaming.javaapi.Processor;
import io.gearpump.streaming.task.StartTime;
import io.gearpump.streaming.task.TaskContext;
import scala.concurrent.duration.FiniteDuration;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({"checkstyle:illegalcatch", "PMD.AvoidCatchingGenericException"})
public class DownloadRulesTask extends RuleEngineTask {

    private static final String TASK_NAME = "downloadRules";
    private static final String START_MSG = "start";
    private static final String CONTINUE_MSG = "continue";
    private final RulesApi rulesApi;
    private static final int TRIGER_INTERVAL = 10; //in seconds
    private Map<String, List<Rule>> componentsRules;

    public DownloadRulesTask(TaskContext context, UserConfig userConfig) {
        this(context, userConfig, new DashboardRulesApi(new DashboardConfigProvider(userConfig)));
    }

    public DownloadRulesTask(TaskContext context, UserConfig userConfig, RulesApi rulesApi) {
        super(context, userConfig);
        this.rulesApi = rulesApi;
    }

    @Override
    public void onStart(StartTime startTime) {
        getLogger().debug("DownloadRulesTask starting...");
        self().tell(new Message(START_MSG, now()), self());
    }

    @Override
    public void onNext(Message message) {
        getLogger().info("Synchronizing dashboard rules...");
        try {
            componentsRules = getComponentsRules();
            getLogger().debug("Components Rules: {}", new Gson().toJson(componentsRules));
            getContext().output(getOutputMessage());
        } catch (InvalidDashboardResponseException e) {
            getLogger().error("Unable to get active rules", e);
        } catch (Exception e) {
            getLogger().error("Unknown error during rules downloading.", e);
        }
        getContext().scheduleOnce(FiniteDuration.create(TRIGER_INTERVAL, TimeUnit.SECONDS), new SelfTrigger());
    }

    private Map<String, List<Rule>> getComponentsRules() throws InvalidDashboardResponseException {
        List<ComponentRulesResponse> componentsRules = rulesApi.getActiveComponentsRules();
        RuleParser ruleParser = new RuleParser(componentsRules);
        Map<String, List<Rule>> result = ruleParser.getComponentRules();

        return result;
    }

    private Message getOutputMessage() {
        return new OutputMessageCreator<Map<String, List<Rule>>>().createOutputMessage(componentsRules);
    }

    public static Processor getProcessor(UserConfig config, int parallelProcessorNumber) {
        return createProcessor(DownloadRulesTask.class, config, parallelProcessorNumber, TASK_NAME);
    }

    private class SelfTrigger extends scala.runtime.AbstractFunction0 {
        @Override
        public Object apply() {
            self().tell(new Message(CONTINUE_MSG, now()), self());
            return null;
        }
    }

}
