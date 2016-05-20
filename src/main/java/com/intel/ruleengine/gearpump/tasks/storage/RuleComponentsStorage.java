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
import com.intel.ruleengine.gearpump.tasks.messages.Rule;
import com.intel.ruleengine.gearpump.tasks.messages.RulesWithObservation;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class RuleComponentsStorage {

    private final RuleConditionsRepository ruleConditionsRepository;
    private final List<RulesWithObservation> rulesWithObservations;

    public RuleComponentsStorage(RuleConditionsRepository ruleConditionsRepository, List<RulesWithObservation> rulesWithObservations) {
        this.ruleConditionsRepository = ruleConditionsRepository;
        this.rulesWithObservations = rulesWithObservations;
    }

    protected List<RulesWithObservation> getFulfilledRules() {
        return filterRulesWithObservation(r -> r.isFulfilled());
    }

    protected List<RulesWithObservation> filterRulesWithObservation(Predicate<Rule> predicate) {
        return rulesWithObservations
                .stream()
                .map(rulesWithObservation -> {
                    List<Rule> fulfilledRules = RuleComponentsStorageManager.filterRules(rulesWithObservation.getRules(), predicate);
                    return new RulesWithObservation(rulesWithObservation.getObservation(), fulfilledRules);
                })
                .collect(Collectors.toList());
    }

    public RuleConditionsRepository getRuleConditionsRepository() {
        return ruleConditionsRepository;
    }

    public List<RulesWithObservation> getRulesWithObservations() {
        return rulesWithObservations;
    }

    public abstract void persist() throws IOException;
}
