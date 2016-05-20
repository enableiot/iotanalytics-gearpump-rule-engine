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
import com.intel.ruleengine.gearpump.apiclients.InvalidDashboardResponseException;
import com.intel.ruleengine.gearpump.apiclients.rules.DashboardRulesApi;
import com.intel.ruleengine.gearpump.apiclients.rules.RulesApi;
import com.intel.ruleengine.gearpump.data.RulesRepository;
import com.intel.ruleengine.gearpump.data.rules.RulesHbaseRepository;
import com.intel.ruleengine.gearpump.tasks.InvalidMessageTypeException;
import com.intel.ruleengine.gearpump.tasks.RuleEngineTask;
import com.intel.ruleengine.gearpump.tasks.messages.Rule;
import com.intel.ruleengine.gearpump.tasks.messages.controllers.InputMessageParser;
import io.gearpump.Message;
import io.gearpump.cluster.UserConfig;
import io.gearpump.streaming.javaapi.Processor;
import io.gearpump.streaming.task.StartTime;
import io.gearpump.streaming.task.TaskContext;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class PersistRulesTask extends RuleEngineTask {

    private static final String TASK_NAME = "persistRules";
    private RulesRepository rulesRepository;
    private final RulesApi rulesApi;

    public PersistRulesTask(TaskContext context, UserConfig userConfig) {
        this(context, userConfig, new RulesHbaseRepository(userConfig), new DashboardRulesApi(new DashboardConfigProvider(userConfig)));
    }

    public PersistRulesTask(TaskContext context, UserConfig userConfig, RulesRepository rulesRepository, RulesApi rulesApi) {
        super(context, userConfig);
        this.rulesRepository = rulesRepository;
        this.rulesApi = rulesApi;
    }

    @Override
    public void onStart(StartTime startTime) {
        getLogger().info("PersistRulesTask start");
        try {
            rulesRepository.createTable();
        } catch (IOException ex) {
            getLogger().warn("Initialization of hbase failed", ex);
        }
    }

    @Override
    public void onNext(Message message) {
        getLogger().info("Persisting dashboard rules...");
        try {
            Map<String, List<Rule>> rules = getInputMessage(message);
            if (!isRulesEmpty(rules)) {
                rulesRepository.putRulesAndRemoveNotExistingOnes(rules);
                rulesApi.markRulesSynchronized(getRulesIds(rules.values()));
            }
        } catch (InvalidMessageTypeException e) {
            getLogger().error("Incorrect message received", e);
        } catch (IOException e) {
            getLogger().error("Persisting error", e);
        } catch (InvalidDashboardResponseException e) {
            getLogger().error("Unable to mark persisted rules as synchronized in Dashboard", e);
        }
    }

    private Set<String> getRulesIds(Collection<List<Rule>> ruleCollection) {
        return ruleCollection.stream()
                .flatMap(rules -> rules.stream())
                .map(rule -> rule.getId())
                .collect(Collectors.toSet());
    }

    private Map<String, List<Rule>> getInputMessage(Message message) throws InvalidMessageTypeException {
        return new InputMessageParser<Map<String, List<Rule>>>().parseInputMapMessage(message);
    }

    private boolean isRulesEmpty(Map<String, List<Rule>> rules) {
        return rules == null || rules.isEmpty();
    }

    public static Processor getProcessor(UserConfig config, int parallelProcessorNumber) {
        return createProcessor(PersistRulesTask.class, config, parallelProcessorNumber, TASK_NAME);
    }
}
