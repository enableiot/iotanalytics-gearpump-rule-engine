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

public class ScanProperties {

    private String ruleId;
    private String componentId;
    private long start;
    private long stop;
    private Long limit;

    public ScanProperties withRuleId(String ruleId) {
        this.ruleId = ruleId;
        return this;
    }

    public ScanProperties withComponentId(String componentId) {
        this.componentId = componentId;
        return this;
    }

    public ScanProperties withStart(long start) {
        this.start = start;
        return this;
    }

    public ScanProperties withStop(long stop) {
        this.stop = stop;
        return this;
    }

    public ScanProperties withStartStop(long start, long stop) {
        this.stop = stop;
        this.start = start;
        return this;
    }

    public ScanProperties withLimit(long limit) {
        this.limit = limit;
        return this;
    }

    void switchStartStop() {
        withStartStop(getStop(), getStart());
    }

    public Long getLimit() {
        return limit;
    }

    public String getRuleId() {
        return ruleId;
    }

    public String getComponentId() {
        return componentId;
    }

    public long getStart() {
        return start;
    }

    public long getStop() {
        return stop;
    }

}
