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
import com.intel.ruleengine.gearpump.data.StatisticsRepository;
import com.intel.ruleengine.gearpump.data.alerts.RuleConditionsHbaseRepository;
import com.intel.ruleengine.gearpump.data.statistics.StatisticsHbaseRepository;
import com.intel.ruleengine.gearpump.tasks.InvalidMessageTypeException;
import com.intel.ruleengine.gearpump.tasks.RuleEngineTask;
import com.intel.ruleengine.gearpump.tasks.messages.Observation;
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
import java.util.stream.Collectors;

public class PersistObservationTask extends RuleEngineTask {

    private static final String TASK_NAME = "persistObservation";

    private final StatisticsRepository statisticsRepository;
    private final RuleConditionsRepository ruleConditionsRepository;
    private List<RulesWithObservation> rulesWithObservation;

    public PersistObservationTask(TaskContext taskContext, UserConfig userConf) {
        this(taskContext, userConf, new StatisticsHbaseRepository(userConf), new RuleConditionsHbaseRepository(userConf));
    }

    public PersistObservationTask(TaskContext taskContext, UserConfig userConf, StatisticsRepository statisticsRepository, RuleConditionsRepository ruleConditionsRepository) {
        super(taskContext, userConf);
        this.statisticsRepository = statisticsRepository;
        this.ruleConditionsRepository = ruleConditionsRepository;
    }

    @Override
    public void onStart(StartTime startTime) {
        super.onStart(startTime);
        try {
            statisticsRepository.createTable();
        } catch (IOException e) {
            getLogger().warn("Initialization of hbase table failed", e);
        }
    }

    @Override
    public void onNext(Message message) {
        try {
            rulesWithObservation = MessageReceiver.build(message).getMessage();

            //Replicate observations data in gearpump's hbase in order for executing statistics aggregations
            List<Observation> observationsForStatisticsRules = filterObservationWithStatisticsRules();
            if (!observationsForStatisticsRules.isEmpty()) {
                statisticsRepository.putObservationForStatisticsRuleCondition(observationsForStatisticsRules);
            }

            //Check if observations data met logical conditions specified in timebased rules (eg. Equal 10, Greater than 5)
            RuleComponentsStorageManager ruleComponentsStorageManager = new RuleComponentsStorageManager(ruleConditionsRepository, rulesWithObservation);
            ruleComponentsStorageManager.persistTimebasedRuleComponents();

            getMessageSender().send(rulesWithObservation);
        } catch (IOException e) {
            getLogger().error("Unable to persistBasicAndStatisticsRuleComponents observation in hbase", e);
        } catch (InvalidMessageTypeException e) {
            getLogger().warn("Incorrect format of message found - {}", message.msg().getClass().getCanonicalName());
        }
    }

    private List<Observation> filterObservationWithStatisticsRules() {
        return rulesWithObservation.stream()
                .filter(r -> hasStatisticConditionsForObservation(r))
                .map(r -> r.getObservation())
                .collect(Collectors.toList());
    }

    private boolean hasStatisticConditionsForObservation(RulesWithObservation rulesWithObservation) {
        return rulesWithObservation.getRules().
                stream().anyMatch(r -> r.hasStatisticsCondition());
    }

    public static Processor getProcessor(UserConfig config, int parallelProcessorNumber) {
        return createProcessor(PersistObservationTask.class, config, parallelProcessorNumber, TASK_NAME);
    }
}
