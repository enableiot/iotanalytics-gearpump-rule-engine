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
import com.intel.ruleengine.gearpump.tasks.messages.Observation;
import com.intel.ruleengine.gearpump.tasks.messages.Rule;
import com.intel.ruleengine.gearpump.tasks.messages.RulesWithObservation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RulesChecker {

    private RulesWithObservation fulfilledRules;
    private final RuleConditionsRepository ruleConditionsRepository;
    private final List<RulesWithObservation> rulesWithObservations;

    public RulesChecker(List<RulesWithObservation> rulesWithObservations, RuleConditionsRepository ruleConditionsRepository) {
        this.ruleConditionsRepository = ruleConditionsRepository;
        this.rulesWithObservations = rulesWithObservations;
    }

    public List<RulesWithObservation> getCompletelyFulfilledRules() {
        List<RulesWithObservation> fulfilledRulesWithObservations = new ArrayList<>();
        for (RulesWithObservation rulesWithObservation : rulesWithObservations) {
            SingleRulesWithObservationChecker rulesChecker = new SingleRulesWithObservationChecker(getPartiallyFulfilledRules(rulesWithObservation));
            fulfilledRules = rulesChecker.getCompletelyFulfilledRules();
            if (hasFulfilledRules()) {
                fulfilledRulesWithObservations.add(fulfilledRules);
            }
        }
        return fulfilledRulesWithObservations;
    }

    private RulesWithObservation getPartiallyFulfilledRules(RulesWithObservation rulesWithObservations) {
        List<Rule> rules = rulesWithObservations.getRules().stream().filter(r -> r.isFulfilled()).collect(Collectors.toList());
        return new RulesWithObservation(rulesWithObservations.getObservation(), rules);
    }

    private boolean hasFulfilledRules() {
        return fulfilledRules != null && fulfilledRules.getRules() != null && fulfilledRules.getRules().size() > 0;
    }

    private class SingleRulesWithObservationChecker {
        private final Observation observation;
        private final List<Rule> rules;

        SingleRulesWithObservationChecker(RulesWithObservation rulesWithObservation) {
            this.rules = rulesWithObservation.getRules();
            this.observation = rulesWithObservation.getObservation();
        }

        public RulesWithObservation getCompletelyFulfilledRules() {
            List<Rule> result = rules.stream()
                    .filter(rule -> new RuleChecker(ruleConditionsRepository, rule, observation).isRuleFulfilled())
                    .collect(Collectors.toList());
            return new RulesWithObservation(observation, result);
        }
    }
}
