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

import com.intel.ruleengine.gearpump.tasks.messages.Rule;
import com.intel.ruleengine.gearpump.tasks.messages.RuleCondition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.function.Predicate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class RuleConditionCheckerTest {

    private RuleConditionsChecker ruleConditionsChecker;

    private String componentId;

    private final String conditionValue = "100.5";

    @Before
    public void init() {
        componentId = IdGenerator.generateId();
    }

    @Test
    public void ruleWithOneConditionShouldBeFulfilledIfConditionIs() {
        Rule rule = RuleCreator.createRuleWithSingleCondition(Operators.EQUAL, conditionValue, componentId);
        boolean result = executeIsRuleFulfilled(rule, getAllFulfilledConditionsPredicate());
        assertTrue(result);
    }

    @Test
    public void ruleWithTwoANDConditionsShouldBeFulfilledIfAllConditionsAre() {
        Rule rule = RuleCreator.createRuleWithTwoConditionsForComponentWithAND(conditionValue, componentId);
        boolean result = executeIsRuleFulfilled(rule, getAllFulfilledConditionsPredicate());
        assertTrue(result);
    }

    @Test
    public void ruleWithTwoORConditionsShouldBeFulfilledIfOneConditionsIs() {
        Rule rule = RuleCreator.createRuleWithTwoConditionsForComponentWithOR(conditionValue, componentId);

        boolean result = executeIsRuleFulfilled(rule, getOnlyOneConditionFulfilledPredicate());
        assertTrue(result);
    }

    @Test
    public void ruleWithTwoANDConditionsShouldNotBeFulfilledIfOnlyOneConditionsIs() {
        Rule rule = RuleCreator.createRuleWithTwoConditionsForComponentWithAND(conditionValue, componentId);

        boolean result = executeIsRuleFulfilled(rule, getOnlyOneConditionFulfilledPredicate());
        assertFalse(result);
    }

    private boolean executeIsRuleFulfilled(Rule rule, Predicate<RuleCondition> predicate) {
        ruleConditionsChecker = new RuleConditionsChecker(rule);
        return ruleConditionsChecker.isRuleFulfilledForComponent(componentId, predicate);
    }

    private Predicate<RuleCondition> getAllFulfilledConditionsPredicate() {
        return  condition -> true;
    }

    private Predicate<RuleCondition> getOnlyOneConditionFulfilledPredicate() {
        return  condition -> condition.getOperator() == Operators.EQUAL;
    }
}
