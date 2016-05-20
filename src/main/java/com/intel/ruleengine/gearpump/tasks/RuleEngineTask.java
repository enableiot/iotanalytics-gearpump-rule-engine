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
package com.intel.ruleengine.gearpump.tasks;

import com.intel.ruleengine.gearpump.tasks.messages.RulesWithObservation;
import com.intel.ruleengine.gearpump.tasks.messages.controllers.MessageSender;
import io.gearpump.Message;
import io.gearpump.cluster.UserConfig;
import io.gearpump.streaming.javaapi.Processor;
import io.gearpump.streaming.task.Task;
import io.gearpump.streaming.task.TaskContext;
import org.slf4j.Logger;

public abstract class RuleEngineTask extends Task {

    private final TaskContext context;
    private final UserConfig userConf;
    private final MessageSender messageSender;


    private final Logger logger = super.LOG();

    public RuleEngineTask(TaskContext taskContext, UserConfig userConf) {
        super(taskContext, userConf);
        this.context = taskContext;
        this.userConf = userConf;
        this.messageSender = new MessageSender(this.context);
    }

    protected long now() {
        return TaskHelper.now();
    }

    protected MessageSender getMessageSender() {
        return messageSender;
    }

    @Override
    public abstract void onNext(Message message);

    public static Processor createProcessor(Class processorClass, UserConfig config, int parallelProcessorNumber, String taskName) {
        return new Processor(processorClass)
                .withConfig(config)
                .withParallelism(parallelProcessorNumber)
                .withDescription(taskName);
    }

    public TaskContext getContext() {
        return context;
    }

    public UserConfig getUserConf() {
        return userConf;
    }

    public Logger getLogger() {
        return logger;
    }

    public static boolean hasFulfilledRules(RulesWithObservation rulesWithObservation) {
        return rulesWithObservation != null && rulesWithObservation.getRules() != null && rulesWithObservation.getRules().size() > 0;
    }
}
