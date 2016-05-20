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

package com.intel.ruleengine.gearpump.rules;

import com.intel.ruleengine.gearpump.data.RuleConditionsRepository;
import com.intel.ruleengine.gearpump.data.alerts.ScanProperties;
import com.intel.ruleengine.gearpump.tasks.messages.Observation;
import com.intel.ruleengine.gearpump.tasks.messages.Rule;
import com.intel.ruleengine.gearpump.tasks.messages.RuleCondition;

import java.io.IOException;


class RuleChecker {

    private final Rule rule;
    private final Observation observation;
    private final RuleConditionsRepository ruleConditionsRepository;

    private final long currentTime;
    //in miliseconds
    private final long timeWindow = 2 * 60 * 1000;

    RuleChecker(RuleConditionsRepository ruleConditionsRepository, Rule rule, Observation observation) {
        this.rule = rule;
        this.observation = observation;
        this.ruleConditionsRepository = ruleConditionsRepository;
        this.currentTime = observation.getOn();
    }

    /**
     * @return
     */
    public boolean isRuleFulfilled() {
        switch (rule.getConditionOperator()) {
            case AND:
                return checkOtherConditions();
            case OR:
            case NONE:
                return true;
            default:
                throw new IllegalArgumentException("Unrecognized condition operator found - " + rule.getConditionOperator());
        }
    }

    private boolean checkOtherConditions() {
        boolean isAllConditionsFulfilled = rule.getConditions().stream()
                .filter(condition -> !condition.getComponentId().equals(observation.getCid()))
                .allMatch(condition -> checkConditionInTimeWindow(condition));
        return isAllConditionsFulfilled;
    }

    private boolean checkConditionInTimeWindow(RuleCondition ruleCondition) {
        try {
            return ruleConditionsRepository.getFulfilledConditionsForComponent(getScanProperties(rule.getId(), ruleCondition.getComponentId())).size() > 0;
        } catch (IOException e) {
            return false;
        }
    }

    private ScanProperties getScanProperties(String ruleId, String componentId) {
        return new ScanProperties().withStart(currentTime - timeWindow)
                .withStop(currentTime)
                .withRuleId(ruleId)
                .withComponentId(componentId);
    }
}
