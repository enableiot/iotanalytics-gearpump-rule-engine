package com.intel.ruleengine.gearpump.graph;

import com.intel.ruleengine.gearpump.tasks.KafkaSourceProcessor;
import com.intel.ruleengine.gearpump.tasks.processors.*;
import io.gearpump.cluster.UserConfig;
import io.gearpump.streaming.javaapi.Processor;

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

class ProcessorsBuilder {

    private final UserConfig userConfig;
    private final ParallelismDefinition parallelismDefinition;

    ProcessorsBuilder(UserConfig userConfig, ParallelismDefinition parallelismDefinition) {
        this.userConfig = userConfig;
        this.parallelismDefinition = parallelismDefinition;
    }

    public Processor getKafkaSource() {
        return new KafkaSourceProcessor(userConfig).getKafkaSourceProcessor(parallelismDefinition.getKafkaSourceProcessorsNumber());
    }

    public Processor getSendAlertsProcessor() {
        return SendAlertsTask.getProcessor(userConfig, parallelismDefinition.getSendAlertsProcessorsNumber());
    }

    public Processor getCheckObservationInRulesProcessor() {
        return CheckObservationInRulesTask.getProcessor(userConfig, parallelismDefinition.getCheckObservationInRulesProcessorsNumber());
    }

    public Processor getDownloadRulesProcessor() {
        return DownloadRulesTask.getProcessor(userConfig, parallelismDefinition.getDownloadRulesProcessorsNumber());
    }

    public Processor getPersistRulesProcessor() {
        return PersistRulesTask.getProcessor(userConfig, parallelismDefinition.getPersistRulesProcessorsNumber());
    }

    public Processor getPersistComponentAlertsProccesor() {
        return PersistComponentAlertsTask.getProcessor(userConfig, parallelismDefinition.getPersistComponentAlertsProccesorsNumber());
    }

    public Processor getCheckRulesProcessor() {
        return CheckRulesTask.getProcessor(userConfig, parallelismDefinition.getCheckRulesProcessorsNumber());
    }

    public Processor getRulesForComponentProcessor() {
        return GetComponentRulesTask.getProcessor(userConfig, parallelismDefinition.getRulesForComponentProcessorsNumber());
    }

    public Processor getPersistObservationProcessor() {
        return PersistObservationTask.getProcessor(userConfig, parallelismDefinition.getPersistObservationProcessorsNumber());
    }
}
