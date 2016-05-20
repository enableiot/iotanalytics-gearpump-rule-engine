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
package com.intel.ruleengine.gearpump.apiclients;

import com.intel.ruleengine.gearpump.util.LogHelper;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;


public final class CustomRestTemplate {


    private static final Logger logger = LogHelper.getLogger(CustomRestTemplate.class);
    private final RestTemplate template;
    private static final int SSL_PORT = 443;

    private CustomRestTemplate(DashboardConfig dashboardConfig) {
        if (!dashboardConfig.isStrictSSL()) {
            template = new RestTemplate(createHttpRequestFactory());
        } else {
            template = new RestTemplate();
        }
    }

    public static CustomRestTemplate build(DashboardConfig dashboardConfig) {
        return new CustomRestTemplate(dashboardConfig);
    }

    public RestTemplate getRestTemplate() {
        return template;
    }

    private ClientHttpRequestFactory createHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory requestFactory = null;
        try {
            requestFactory = new HttpComponentsClientHttpRequestFactory();
            switchOffSSLVerification(requestFactory.getHttpClient());
        } catch (GeneralSecurityException e) {
            logger.error("Error during disabling strict ssl certificate verification", e);
        }
        return requestFactory;
    }

    private HttpClient switchOffSSLVerification(HttpClient httpClient) throws GeneralSecurityException {
        TrustStrategy acceptingTrustStrategy = new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] certificate, String authType) {
                return true;
            }
        };

        SSLSocketFactory socketFactory = new SSLSocketFactory(acceptingTrustStrategy, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", SSL_PORT, socketFactory));

        return httpClient;
    }
}
