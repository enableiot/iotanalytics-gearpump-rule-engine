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

package com.intel.ruleengine.gearpump.tasks.storage;

import com.intel.ruleengine.gearpump.data.RuleConditionsRepository;
import com.intel.ruleengine.gearpump.rules.RuleConditionsChecker;
import com.intel.ruleengine.gearpump.rules.conditions.BasicConditionChecker;
import com.intel.ruleengine.gearpump.tasks.messages.Observation;
import com.intel.ruleengine.gearpump.tasks.messages.Rule;
import com.intel.ruleengine.gearpump.tasks.messages.RuleCondition;
import com.intel.ruleengine.gearpump.tasks.messages.RulesWithObservation;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class TimebasedRuleComponentsStorage extends RuleComponentsStorage {

    TimebasedRuleComponentsStorage(RuleConditionsRepository ruleConditionsRepository, List<RulesWithObservation> rulesWithObservation) {
        super(ruleConditionsRepository, rulesWithObservation);
    }

    @Override
    public void persist() throws IOException {
        getRuleConditionsRepository().putFulfilledConditionsForObservation(getFulfilledRules());
        persistTimebaseComponents(true);
        persistTimebaseComponents(false);
    }

    private void persistTimebaseComponents(boolean fulfilled) throws IOException {
        getRuleConditionsRepository().putTimebasedRuleComponents(createRulesWithObservation(fulfilled), fulfilled);
    }

    private List<RulesWithObservation> createRulesWithObservation(boolean fulfilled) {
        return getRulesWithObservations()
                .stream()
                .map(ruleWithObservation -> {
                    Observation observation = ruleWithObservation.getObservation();
                    Predicate<Rule> predicate = rule -> hasSingleTimabasedConditionFulfilled(observation, rule);
                    if (!fulfilled) {
                        predicate = rule -> !hasSingleTimabasedConditionFulfilled(observation, rule);
                    }
                    return new RulesWithObservation(ruleWithObservation.getObservation(), filterRules(ruleWithObservation.getRules(), predicate));
                })
                .collect(Collectors.toList());
    }

    private List<Rule> filterRules(List<Rule> rules, Predicate<Rule> condition) {
        return rules.stream().filter(condition).collect(Collectors.toList());
    }

    private boolean hasSingleTimabasedConditionFulfilled(Observation observation, Rule rule) {
        return new RuleConditionsChecker(rule)
                .isRuleFulfilledForComponent(observation.getCid(), hasSingleTimabasedConditionFulfilled(observation));
    }

    private Predicate<RuleCondition> hasSingleTimabasedConditionFulfilled(Observation observation) {
        return condition -> condition.isTimebased() && new BasicConditionChecker(condition).isConditionFulfilled(observation);
    }
}
