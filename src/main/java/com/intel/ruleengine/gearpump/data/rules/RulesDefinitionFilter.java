package com.intel.ruleengine.gearpump.data.rules;

import com.google.common.collect.Lists;
import com.intel.ruleengine.gearpump.tasks.messages.Rule;
import com.intel.ruleengine.gearpump.util.LogHelper;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Copyright (c) 2015 Intel Corporation
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class RulesDefinitionFilter {

    private final Set<Rule> mergedRulesDefinition;
    private final Logger logger = LogHelper.getLogger(RulesDefinitionFilter.class);

    RulesDefinitionFilter(List<Rule> rulesDefinition) {
        mergedRulesDefinition = new HashSet<>();
        if (rulesDefinition != null) {
            this.mergedRulesDefinition.addAll(rulesDefinition);
        }
    }

    public void merge(Stream<Rule> newRulesDefinition) {
        newRulesDefinition.forEach(r -> {
            if (r.isActive()) {
                mergedRulesDefinition.add(r);
            } else {
                boolean result = mergedRulesDefinition.remove(r);
                if (!result) {
                    logger.warn("Rule - {} with status DELETED not found in hbase!", r.getId());
                }
            }
        });
    }

    public List<Rule> getMergedRulesDefinition() {
        return Lists.newArrayList(mergedRulesDefinition);
    }
}
