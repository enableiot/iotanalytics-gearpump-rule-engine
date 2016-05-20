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
import com.intel.ruleengine.gearpump.data.StatisticsRepository;
import com.intel.ruleengine.gearpump.rules.conditions.ConditionChecker;
import com.intel.ruleengine.gearpump.rules.conditions.ConditionCheckerFactory;
import com.intel.ruleengine.gearpump.tasks.messages.Observation;
import com.intel.ruleengine.gearpump.tasks.messages.Rule;
import com.intel.ruleengine.gearpump.tasks.messages.RuleCondition;
import com.intel.ruleengine.gearpump.tasks.messages.RulesWithObservation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;


public class RulesObservationChecker {

    private RulesWithObservation fulfilledRules;
    private final RuleConditionsRepository ruleConditionsRepository;
    private final Observation observation;
    private final String observationComponentId;
    private final List<Rule> rules;
    private final StatisticsRepository statisticsRepository;

    public RulesObservationChecker(List<Rule> rules, Observation observation, RuleConditionsRepository ruleConditionsRepository, StatisticsRepository statisticsRepository) {
        this.rules = rules;
        this.observation = observation;
        this.ruleConditionsRepository = ruleConditionsRepository;
        this.observationComponentId = observation.getCid();
        this.statisticsRepository = statisticsRepository;
    }

    public RulesObservationChecker(RulesWithObservation rulesWithObservation, RuleConditionsRepository ruleConditionsRepository, StatisticsRepository statisticsRepository) {
        this(rulesWithObservation.getRules(), rulesWithObservation.getObservation(), ruleConditionsRepository, statisticsRepository);
    }

    public RulesWithObservation checkRulesForObservation() {
        fulfilledRules = null;

        //TODO consider excluding rules with statistics conditions from here. Such rules processing can be time-consuming.
        //Such rules can be processed in another interval-triggered Task
        for (Rule rule : rules) {
            if (isRuleFulfilledForComponent(rule)) {
                rule.setFulfilled(true);
            }
        }
        createFulfilledRules(rules);
        return fulfilledRules;
    }

    private boolean isRuleFulfilledForComponent(Rule rule) {
        return new RuleConditionsChecker(rule).isRuleFulfilledForComponent(observationComponentId, checkConditionPredicate());
    }

    private Predicate<RuleCondition> checkConditionPredicate() {
        return condition -> getConditionChecker(condition).isConditionFulfilled(observation);
    }

    private ConditionChecker getConditionChecker(RuleCondition ruleCondition) {
        return ConditionCheckerFactory.getConditionChecker(ruleCondition, ruleConditionsRepository, statisticsRepository);
    }


    private void createFulfilledRules(List<Rule> rules) {
        if (rules != null && !rules.isEmpty()) {
            fulfilledRules = new RulesWithObservation(observation, rules);
        } else {
            fulfilledRules = new RulesWithObservation(observation, new ArrayList<>());
        }
    }
}
