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

import com.intel.ruleengine.gearpump.tasks.messages.RuleCondition;

public abstract class BaseConditionChecker implements ConditionChecker {

    private final RuleCondition ruleCondition;
    private final long timeWindowLength; //in milliseconds


    public static final int MILLISECONDS_MULTIPLIER = 1000;

    public BaseConditionChecker(RuleCondition ruleCondition) {
        this.ruleCondition = ruleCondition;
        this.timeWindowLength = getTimeWindowLengthInMilliSeconds(ruleCondition.getTimeLimit());
    }

    private long getTimeWindowLengthInMilliSeconds(Long timeLimitInSeconds) {
        if (timeLimitInSeconds == null) {
            return 0;
        }
        return timeLimitInSeconds * MILLISECONDS_MULTIPLIER;
    }

    protected RuleCondition getRuleCondition() {
        return ruleCondition;
    }

    protected long getTimeWindowLength() {
        return timeWindowLength;
    }

}
