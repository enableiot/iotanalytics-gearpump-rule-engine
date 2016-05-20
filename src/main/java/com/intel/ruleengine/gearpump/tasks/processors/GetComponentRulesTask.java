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

import com.intel.ruleengine.gearpump.data.RulesRepository;
import com.intel.ruleengine.gearpump.data.rules.RulesHbaseRepository;
import com.intel.ruleengine.gearpump.tasks.InvalidMessageTypeException;
import com.intel.ruleengine.gearpump.tasks.RuleEngineTask;
import com.intel.ruleengine.gearpump.tasks.messages.Observation;
import com.intel.ruleengine.gearpump.tasks.messages.Rule;
import com.intel.ruleengine.gearpump.tasks.messages.RulesWithObservation;
import com.intel.ruleengine.gearpump.tasks.messages.controllers.KafkaMessageConverter;
import io.gearpump.Message;
import io.gearpump.cluster.UserConfig;
import io.gearpump.streaming.javaapi.Processor;
import io.gearpump.streaming.task.TaskContext;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class GetComponentRulesTask extends RuleEngineTask {

    private static final String TASK_NAME = "getRulesForComponent";
    private List<Observation> observations;

    private RulesRepository rulesRepository;

    public GetComponentRulesTask(TaskContext context, UserConfig userConfig) {
        this(context, userConfig, new RulesHbaseRepository(userConfig));
    }

    public GetComponentRulesTask(TaskContext context, UserConfig userConfig, RulesRepository rulesRepository) {
        super(context, userConfig);
        this.rulesRepository = rulesRepository;
    }

    @Override
    public void onNext(Message message) {
        try {
            getLogger().info("GetRulesTask started");
            observations = getInputMessage(message);
            sendObservations();
        } catch (InvalidMessageTypeException e) {
            getLogger().warn("Incorrect format of message found - " + message.msg().getClass().getCanonicalName());
        } catch (IOException e) {
            getLogger().error("Error during searching rules in hbase - ", e);
        }
    }

    private void sendObservations() throws IOException {
        List<RulesWithObservation> rulesWithObservations = getRulesWithObservation(observations);

        List<RulesWithObservation> observationsWithActiveRules = rulesWithObservations.stream()
                .filter(r -> hasObservationRules(r))
                .collect(Collectors.toList());

        getMessageSender().send(observationsWithActiveRules);
    }

    private List<RulesWithObservation> getRulesWithObservation(List<Observation> observations) throws IOException {
        Set<String> componentsIds = observations.stream()
                .map(o -> o.getCid())
                .collect(Collectors.toSet());

        String accountId = observations.stream()
                .findFirst().get().getAid();

        Map<String, List<Rule>> componentsRules = rulesRepository.getComponentsRules(accountId, componentsIds);

        return observations.stream()
                .map(observation -> new RulesWithObservation(observation, componentsRules.get(observation.getCid())))
                .collect(Collectors.toList());
    }

    private boolean hasObservationRules(RulesWithObservation rulesWithObservation) {
        return rulesWithObservation.getRules() != null && rulesWithObservation.getRules().size() > 0;
    }

    private List<Observation> getInputMessage(Message message) throws InvalidMessageTypeException {
        return new KafkaMessageConverter(message).toObservationList();
    }

    public static Processor getProcessor(UserConfig config, int parallelProcessorNumber) {
        return createProcessor(GetComponentRulesTask.class, config, parallelProcessorNumber, TASK_NAME);
    }
}
