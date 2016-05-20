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

package com.intel.ruleengine.gearpump.apiclients.alerts;

import com.intel.ruleengine.gearpump.tasks.messages.Observation;

import java.util.ArrayList;
import java.util.List;

class Condition {
    private List<Component> components;

    Condition() {
    }

    Condition(Observation observation) {
        components = new ArrayList<>();
        components.add(new Component(observation.getCid(), observation.getOn(), observation.getValue()));
    }

    public List<Component> getComponents() {
        return components;
    }

    public void setComponents(List<Component> components) {
        this.components = components;
    }

    class Component {
        private String componentId;
        private List<Points> valuePoints;

        Component() {
        }

        Component(String componentId, long timestamp, String value) {
            this.componentId = componentId;
            valuePoints = new ArrayList<>();
            valuePoints.add(new Points(value, timestamp));
        }

        public String getComponentId() {
            return componentId;
        }

        public void setComponentId(String componentId) {
            this.componentId = componentId;
        }

        public List<Points> getValuePoints() {
            return valuePoints;
        }

        public void setValuePoints(List<Points> valuePoints) {
            this.valuePoints = valuePoints;
        }
    }

    class Points {
        private String value;
        private long timestamp;

        Points() {
        }

        Points(String value, long timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
