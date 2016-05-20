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

package com.intel.ruleengine.gearpump.rules.conditions;

import com.intel.ruleengine.gearpump.rules.DataType;
import com.intel.ruleengine.gearpump.rules.IdGenerator;
import com.intel.ruleengine.gearpump.rules.Operators;
import com.intel.ruleengine.gearpump.rules.RuleConditionCreator;
import com.intel.ruleengine.gearpump.rules.conditions.functions.IllegalOperatorException;
import com.intel.ruleengine.gearpump.tasks.messages.Rule;
import com.intel.ruleengine.gearpump.tasks.messages.RuleCondition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class ConditionFunctionCheckerTest {

    private ConditionFunctionChecker conditionFunctionChecker;

    private RuleCondition ruleCondition;

    private final String ruleId = IdGenerator.generateId();

    private final String componentId = IdGenerator.generateId();

    private final RuleConditionCreator ruleConditionCreator = new RuleConditionCreator(ruleId);

    private List<String> conditionValues;

    private final int conditionValue = 4;

    @Before
    public void init() {
        conditionValues = Arrays.asList(String.valueOf(conditionValue));
    }

    @Test
    public void equalNumberDataTypeConditionShouldBeFulfilledIfObservationValueEqualConditionValue() {
        createConditionForNumberDateType(Operators.EQUAL, conditionValues);

        assertTrue(conditionFunctionChecker.isConditionFulfilled(conditionValues.get(0)));
        assertFalse(conditionFunctionChecker.isConditionFulfilled(String.valueOf(conditionValue + 1)));
    }

    @Test
    public void notEqualNumberDataTypeConditionShouldBeFulfilledIfObservationValueNotEqualConditionValue() {
        createConditionForNumberDateType(Operators.NOT_EQUAL, conditionValues);

        assertTrue(conditionFunctionChecker.isConditionFulfilled(String.valueOf(conditionValue + 1)));
        assertFalse(conditionFunctionChecker.isConditionFulfilled(String.valueOf(conditionValue)));
    }

    @Test
    public void greaterNumberDataTypeConditionShouldBeFulfilledIfObservationValueIsGraterThanConditionValue() {
        createConditionForNumberDateType(Operators.GREATER, conditionValues);

        assertTrue(conditionFunctionChecker.isConditionFulfilled(String.valueOf(conditionValue + 1)));
        assertFalse(conditionFunctionChecker.isConditionFulfilled(String.valueOf(conditionValue - 1)));
    }

    @Test
    public void greaterEqNumberDataTypeConditionShouldBeFulfilledIfObservationValueIsGraterOrEqConditionValue() {
        createConditionForNumberDateType(Operators.GREATER_EQ, conditionValues);

        assertTrue(conditionFunctionChecker.isConditionFulfilled(String.valueOf(conditionValue + 1)));
        assertTrue(conditionFunctionChecker.isConditionFulfilled(conditionValues.get(0)));
        assertFalse(conditionFunctionChecker.isConditionFulfilled(String.valueOf(conditionValue - 1)));
    }

    @Test
    public void lowerNumberDataTypeConditionShouldBeFulfilledIfObservationValueIsLowerThanConditionValue() {
        createConditionForNumberDateType(Operators.LOWER, conditionValues);

        assertTrue(conditionFunctionChecker.isConditionFulfilled(String.valueOf(conditionValue -1)));
        assertFalse(conditionFunctionChecker.isConditionFulfilled(String.valueOf(conditionValue + 1)));
    }

    @Test
    public void lowerEqNumberDataTypeConditionShouldBeFulfilledIfObservationValueIsLowerOrEqConditionValue() {
        createConditionForNumberDateType(Operators.LOWER_EQ, conditionValues);

        assertTrue(conditionFunctionChecker.isConditionFulfilled(String.valueOf(conditionValue -1)));
        assertTrue(conditionFunctionChecker.isConditionFulfilled(String.valueOf(conditionValue)));
        assertFalse(conditionFunctionChecker.isConditionFulfilled(String.valueOf(conditionValue + 1)));
    }

    @Test
    public void notBetweenNumberDataTypeConditionShouldBeFulfilledIfObservationValueIsNoBetweenConditionValues() {
        List<String> conditionsValues = Arrays.asList("4", "6");
        createConditionForNumberDateType(Operators.NOT_BETWEEN, conditionsValues);

        assertTrue(conditionFunctionChecker.isConditionFulfilled("7"));
        assertTrue(conditionFunctionChecker.isConditionFulfilled("3"));
        assertFalse(conditionFunctionChecker.isConditionFulfilled("5"));
    }

    @Test
    public void betweenNumberDataTypeConditionShouldBeFulfilledIfObservationValueIsBetweenConditionValues() {
        List<String> conditionValues = Arrays.asList("4", "6");
        createConditionForNumberDateType(Operators.BETWEEN, conditionValues);

        assertTrue(conditionFunctionChecker.isConditionFulfilled(conditionValues.get(0)));
        assertTrue(conditionFunctionChecker.isConditionFulfilled(conditionValues.get(1)));
        assertTrue(conditionFunctionChecker.isConditionFulfilled("5"));
        assertFalse(conditionFunctionChecker.isConditionFulfilled("7"));
    }

    @Test
    public void likeNumberDataTypeConditionShouldAlwaysReturnFalse() {
        createConditionForNumberDateType(Operators.LIKE, conditionValues);

        assertFalse(conditionFunctionChecker.isConditionFulfilled(conditionValues.get(0)));
    }

    @Test
    public void equalStringDataTypeConditionShouldBeFulfilledIfObservationValueEqualConditionValue() {
        createConditionForStringDateType(Operators.EQUAL, conditionValues);

        assertTrue(conditionFunctionChecker.isConditionFulfilled(conditionValues.get(0)));
        assertFalse(conditionFunctionChecker.isConditionFulfilled(conditionValues.get(0) + "suffix"));
    }

    @Test
    public void notEqualStringDataTypeConditionShouldBeFulfilledIfObservationNotEqualConditionValue() {
        createConditionForStringDateType(Operators.NOT_EQUAL, conditionValues);

        assertFalse(conditionFunctionChecker.isConditionFulfilled(conditionValues.get(0)));
        assertTrue(conditionFunctionChecker.isConditionFulfilled(conditionValues.get(0) + "not_equal"));
    }

    @Test
    public void likeStringDataTypeConditionShouldBeFulfilledIfObservationIsLikeConditionValue() {
        List<String> conditionValues = Arrays.asList("alamakota");
        createConditionForStringDateType(Operators.LIKE, conditionValues);

        assertFalse(conditionFunctionChecker.isConditionFulfilled("differentString"));
        assertTrue(conditionFunctionChecker.isConditionFulfilled(conditionValues.get(0).substring(0, 4)));
    }

    @Test
    public void notSupportedStringDataTypeConditionShouldBeNotBeFulfilled() {
        createConditionForStringDateType(Operators.BETWEEN, conditionValues);
        assertFalse(conditionFunctionChecker.isConditionFulfilled(conditionValues.get(0)));

        createConditionForStringDateType(Operators.NOT_BETWEEN, conditionValues);
        assertFalse(conditionFunctionChecker.isConditionFulfilled(conditionValues.get(0)));

        createConditionForStringDateType(Operators.GREATER, conditionValues);
        assertFalse(conditionFunctionChecker.isConditionFulfilled(conditionValues.get(0)));

        createConditionForStringDateType(Operators.GREATER_EQ, conditionValues);
        assertFalse(conditionFunctionChecker.isConditionFulfilled(conditionValues.get(0)));

        createConditionForStringDateType(Operators.LOWER, conditionValues);
        assertFalse(conditionFunctionChecker.isConditionFulfilled(conditionValues.get(0)));

        createConditionForStringDateType(Operators.LOWER_EQ, conditionValues);
        assertFalse(conditionFunctionChecker.isConditionFulfilled(conditionValues.get(0)));
    }

    @Test
    public void equalBooleanDataTypeConditionShouldBeFulfilledIfObservationValueEqualConditionValue() {
        List<String> conditionValues = Arrays.asList("true");
        createConditionForBooleanDateType(Operators.EQUAL, conditionValues);

        assertTrue(conditionFunctionChecker.isConditionFulfilled(conditionValues.get(0)));
        assertFalse(conditionFunctionChecker.isConditionFulfilled("false"));
    }

    @Test
    public void notEqualBooleanDataTypeConditionShouldBeFulfilledIfObservationValueIsNotEqualConditionValue() {
        List<String> conditionValues = Arrays.asList("true");
        createConditionForBooleanDateType(Operators.NOT_EQUAL, conditionValues);

        assertTrue(conditionFunctionChecker.isConditionFulfilled("false"));
        assertFalse(conditionFunctionChecker.isConditionFulfilled(conditionValues.get(0)));
    }

    @Test
    public void notSupportedBooleanDataTypeConditionShouldBeNotBeFulfilled() {
        List<String> conditionValues = Arrays.asList("true");
        createConditionForBooleanDateType(Operators.BETWEEN, conditionValues);
        assertFalse(conditionFunctionChecker.isConditionFulfilled(conditionValues.get(0)));

        createConditionForBooleanDateType(Operators.NOT_BETWEEN, conditionValues);
        assertFalse(conditionFunctionChecker.isConditionFulfilled(conditionValues.get(0)));

        createConditionForBooleanDateType(Operators.GREATER, conditionValues);
        assertFalse(conditionFunctionChecker.isConditionFulfilled(conditionValues.get(0)));

        createConditionForBooleanDateType(Operators.GREATER_EQ, conditionValues);
        assertFalse(conditionFunctionChecker.isConditionFulfilled(conditionValues.get(0)));

        createConditionForBooleanDateType(Operators.LOWER, conditionValues);
        assertFalse(conditionFunctionChecker.isConditionFulfilled(conditionValues.get(0)));

        createConditionForBooleanDateType(Operators.LOWER_EQ, conditionValues);
        assertFalse(conditionFunctionChecker.isConditionFulfilled(conditionValues.get(0)));

        createConditionForBooleanDateType(Operators.LIKE, conditionValues);
        assertFalse(conditionFunctionChecker.isConditionFulfilled(conditionValues.get(0)));
    }


    private void createConditionForNumberDateType(Operators operator, List<String> values) {
        createConditionForDateType(operator, values, DataType.NUMBER);
    }

    private void createConditionForStringDateType(Operators operator, List<String> values) {
        createConditionForDateType(operator, values, DataType.STRING);
    }

    private void createConditionForBooleanDateType(Operators operator, List<String> values) {
        createConditionForDateType(operator, values, DataType.BOOLEAN);
    }

    private void createConditionForDateType(Operators operator, List<String> values, DataType dataType) {
        ruleCondition = ruleConditionCreator.createBasicRuleCondition(componentId, operator, values, dataType);
        conditionFunctionChecker = new ConditionFunctionChecker(ruleCondition);
    }
}
