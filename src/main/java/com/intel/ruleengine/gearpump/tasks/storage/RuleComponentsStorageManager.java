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

public class RuleComponentsStorageManager {

    private final RuleConditionsRepository ruleConditionsRepository;
    private final List<RulesWithObservation> rulesWithObservations;

    private List<RulesWithObservation> basicStatisticsRulesWithObservation;
    private List<RulesWithObservation> timebasedRulesWithObservation;

    public RuleComponentsStorageManager(RuleConditionsRepository ruleConditionsRepository, List<RulesWithObservation> rulesWithObservations) {
        this.ruleConditionsRepository = ruleConditionsRepository;
        this.rulesWithObservations = rulesWithObservations;
        buildFilteredRules();
    }

    public void persistBasicAndStatisticsRuleComponents() throws IOException {
        new BasicRuleComponentsStorage(ruleConditionsRepository, basicStatisticsRulesWithObservation).persist();
    }

    public void persistTimebasedRuleComponents() throws IOException {
        new TimebasedRuleComponentsStorage(ruleConditionsRepository, timebasedRulesWithObservation).persist();
    }

    public static List<Rule> filterRules(List<Rule> rules, Predicate<Rule> condition) {
        return rules.stream().filter(condition).collect(Collectors.toList());
    }

    private void buildFilteredRules() {
        basicStatisticsRulesWithObservation = getBasicAndStatisticsRules();
        timebasedRulesWithObservation = getTimebasedRules();
    }

    private List<RulesWithObservation> getBasicAndStatisticsRules() {
        return filterRulesWithObservations(r -> !r.hasTimebasedCondition());
    }

    private List<RulesWithObservation> filterRulesWithObservations(Predicate<Rule> condition) {
        return rulesWithObservations.stream()
                .map(rulesWithObservation ->
                        new RulesWithObservation(rulesWithObservation.getObservation(), filterRules(rulesWithObservation.getRules(), condition)))
                .collect(Collectors.toList());
    }

    private List<RulesWithObservation> getTimebasedRules() {
        return filterRulesWithObservations(r -> r.hasTimebasedCondition());
    }
}
