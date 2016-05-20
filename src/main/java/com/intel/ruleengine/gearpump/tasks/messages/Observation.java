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

package com.intel.ruleengine.gearpump.tasks.messages;

import com.google.common.base.Objects;

import java.util.List;
import java.util.Map;


public class Observation {

    private String aid;
    private String cid;
    private Long on;
    private Long systemOn;
    private String value;
    private List<Double> loc;
    private Map<String, String> attributes;

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public Long getOn() {
        return on;
    }

    public void setOn(Long on) {
        this.on = on;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<Double> getLoc() {
        return loc;
    }

    public void setLoc(List<Double> loc) {
        this.loc = loc;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public Long getSystemOn() {
        return systemOn;
    }

    public void setSystemOn(Long systemOn) {
        this.systemOn = systemOn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Observation other = (Observation) o;

        return Objects.equal(this.aid, other.aid) && Objects
                .equal(this.cid, other.cid) && Objects
                .equal(this.on, other.on) && Objects
                .equal(this.systemOn, other.systemOn) && Objects
                .equal(this.value, other.value) && Objects
                .equal(this.loc, other.loc) && Objects
                .equal(this.attributes, other.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(aid, cid, on, systemOn, value, loc, attributes);
    }
}
