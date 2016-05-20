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

package com.intel.ruleengine.gearpump.apiclients.rules;

import java.util.List;

class RuleRequest {

    private List<String> status;

    private String synchronizationStatus;

    RuleRequest() {

    }

    RuleRequest(List<String> status, String synchronizationStatus) {
        this.status = status;
        this.synchronizationStatus = synchronizationStatus;
    }

    public void setStatus(List<String> status) {
        this.status = status;
    }

    public List<String> getStatus() {
        return status;
    }

    public String getSynchronizationStatus() {
        return synchronizationStatus;
    }

    public void setSynchronizationStatus(String synchronizationStatus) {
        this.synchronizationStatus = synchronizationStatus;
    }


}
