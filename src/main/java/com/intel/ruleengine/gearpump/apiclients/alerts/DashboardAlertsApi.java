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

import com.intel.ruleengine.gearpump.apiclients.ApiClientHelper;
import com.intel.ruleengine.gearpump.apiclients.CustomRestTemplate;
import com.intel.ruleengine.gearpump.apiclients.DashboardConfigProvider;
import com.intel.ruleengine.gearpump.apiclients.InvalidDashboardResponseException;
import com.intel.ruleengine.gearpump.tasks.messages.Observation;
import com.intel.ruleengine.gearpump.tasks.messages.Rule;
import com.intel.ruleengine.gearpump.tasks.messages.RulesWithObservation;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;


public class DashboardAlertsApi implements AlertsApi {

    private final String url;
    private final String token;
    private static final String PATH = "/v1/api/alerts";
    private List<RulesWithObservation> rulesWithObservations;

    private final RestTemplate template;

    public DashboardAlertsApi(DashboardConfigProvider dashboardConfig) {
        this(dashboardConfig, CustomRestTemplate.build(dashboardConfig).getRestTemplate());
    }

    public DashboardAlertsApi(DashboardConfigProvider dashboardConfig, RestTemplate restTemplate) {
        token = dashboardConfig.getToken();
        url = dashboardConfig.getUrl() + PATH;
        template = restTemplate;
    }

    private String getToken() {
        return token;
    }

    @Override
    public void pushAlert(List<RulesWithObservation> rulesWithObservation) throws InvalidDashboardResponseException {
        this.rulesWithObservations = rulesWithObservation;

        HttpHeaders headers = ApiClientHelper.getHttpHeaders(getToken());
        HttpEntity req = new HttpEntity<>(createAlertBody(), headers);

        try {
            ResponseEntity<String> resp = template.exchange(url, HttpMethod.POST, req, String.class);
            if (resp.getStatusCode() != HttpStatus.OK) {
                throw new InvalidDashboardResponseException("Invalid response - " + resp.getStatusCode());
            }
        } catch (RestClientException e) {
            throw new InvalidDashboardResponseException("Unknown dashboard response error.", e);
        }
    }

    private AlertRequest createAlertBody() {
        AlertRequest body = new AlertRequest();
        body.setMsgType(AlertRequest.MSG_TYPE);

        List<Alert> alerts = new ArrayList<>();

        rulesWithObservations.forEach(rules -> {
            rules.getRules().forEach(rule -> {
                alerts.add(createAlertForRule(rule, rules.getObservation()));
            });
        });

        body.setData(alerts);

        return body;
    }

    private Alert createAlertForRule(Rule rule, Observation observation) {
        Alert alert = new Alert();
        alert.setAccountId(rule.getAccountId());
        alert.setRuleId(rule.getId());
        alert.setTimestamp(System.currentTimeMillis());
        alert.setConditions(createAlertCondition(observation));
        alert.setSystemOn(observation.getSystemOn());
        return alert;
    }

    private List<Condition> createAlertCondition(Observation observation) {
        List<Condition> alertConditions = new ArrayList<>();
        alertConditions.add(new Condition(observation));
        return alertConditions;
    }
}
