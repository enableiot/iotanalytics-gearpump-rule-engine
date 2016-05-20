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

import com.intel.ruleengine.gearpump.tasks.messages.Observation;
import com.intel.ruleengine.gearpump.tasks.messages.RuleCondition;

public class BasicConditionChecker extends BaseConditionChecker implements ConditionChecker {

    private ConditionFunctionChecker conditionFunctionChecker;

    public BasicConditionChecker(RuleCondition ruleCondition) {
        super(ruleCondition);
        this.conditionFunctionChecker = new ConditionFunctionChecker(ruleCondition);
    }

    @Override
    public boolean isConditionFulfilled(Observation observation) {
        return conditionFunctionChecker.isConditionFulfilled(observation.getValue());
    }
}
