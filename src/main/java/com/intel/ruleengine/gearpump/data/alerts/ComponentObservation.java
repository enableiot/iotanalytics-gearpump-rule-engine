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

package com.intel.ruleengine.gearpump.data.alerts;

public final class ComponentObservation {

    private final long timestamp;
    private final String componentId;

    public ComponentObservation(String componentId, long timestamp) {
        this.componentId = componentId;
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getComponentId() {
        return componentId;
    }

    public static ComponentObservation fromResultRow(byte[] resultRow) {
        return new ComponentObservation(RuleComponentsRowCreator.parseComponentId(resultRow), RuleComponentsRowCreator.parseTimestamp(resultRow));
    }


}
