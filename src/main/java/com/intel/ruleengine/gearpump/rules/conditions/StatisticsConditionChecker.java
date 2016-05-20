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

import com.intel.ruleengine.gearpump.data.StatisticsRepository;
import com.intel.ruleengine.gearpump.data.alerts.ScanProperties;
import com.intel.ruleengine.gearpump.data.statistics.StatisticsValues;
import com.intel.ruleengine.gearpump.tasks.messages.Observation;
import com.intel.ruleengine.gearpump.tasks.messages.RuleCondition;
import com.intel.ruleengine.gearpump.util.LogHelper;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StatisticsConditionChecker extends BaseConditionChecker implements ConditionChecker {

    private double average;
    private double standardDeviation;
    private List<String> conditionValues;
    private final StatisticsRepository statisticsRepository;
    private Observation observation;
    private static final Logger logger = LogHelper.getLogger(StatisticsConditionChecker.class);

    public StatisticsConditionChecker(RuleCondition ruleCondition, StatisticsRepository statisticsRepository) {
        super(ruleCondition);
        this.statisticsRepository = statisticsRepository;
    }

    @Override
    public boolean isConditionFulfilled(Observation observation) {
        this.observation = observation;
        try {
            if (!hasRequiredObservationCount()) {
                return false;
            }
            calculateStatistics();
            buildConditionValues();
            ConditionFunctionChecker conditionFunctionChecker = createConditionChecker();
            logger.debug("Avg - {}, Std - {}, Values - {}", average, standardDeviation, Arrays.toString(conditionValues.toArray()));
            return conditionFunctionChecker.isConditionFulfilled(observation.getValue());
        } catch (IOException e) {
            logger.error("Unable to verify statistics condition for componentId - {}", observation.getCid(), e);
            return false;
        }
    }

    private boolean hasRequiredObservationCount() throws IOException {
        double count = statisticsRepository.getObservationCount(createScanProperties());
        return Double.compare(count, getRuleCondition().getMinimalObservationCountInTimeWindow()) >= 0;
    }

    private void calculateStatistics() throws IOException {
        StatisticsValues statisticsValues = statisticsRepository.getStatisticsValuesForObservation(createScanProperties());
        average = statisticsValues.getAverage();
        standardDeviation = statisticsValues.getStandardDeviation();
    }

    private ScanProperties createScanProperties() {
        return new ScanProperties()
                .withComponentId(observation.getCid())
                .withStart(getTimeWindowStart())
                .withStop(getTimeWindowEnd());
    }

    private long getTimeWindowStart() {
        return observation.getOn() - getTimeWindowLength();
    }

    private long getTimeWindowEnd() {
        return observation.getOn();
    }

    private void buildConditionValues() {
        conditionValues = getRuleCondition().getValues().stream()
                .map(value -> calculateValue(value))
                .collect(Collectors.toList());
    }

    private ConditionFunctionChecker createConditionChecker() {
        return new ConditionFunctionChecker(getRuleCondition().getOperator(), conditionValues, getRuleCondition().getComponentDataType());
    }

    private String calculateValue(String value) {
        return calculateStatisticsConditionValue(Double.valueOf(value), average, standardDeviation);
    }

    public static String calculateStatisticsConditionValue(double value, double average, double standardDeviation) {
        return String.valueOf(value * standardDeviation + average);
    }
}
