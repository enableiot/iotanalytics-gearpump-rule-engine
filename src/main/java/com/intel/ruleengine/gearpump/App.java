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

package com.intel.ruleengine.gearpump;

import com.intel.ruleengine.gearpump.graph.GraphBuilder;
import com.intel.ruleengine.gearpump.graph.GraphConfig;
import io.gearpump.cluster.UserConfig;
import io.gearpump.cluster.client.ClientContext;
import io.gearpump.streaming.javaapi.StreamApplication;

public class App {

    public static void main(String... args) {

        ClientContext context = ClientContext.apply();
        GraphConfig graphConfig = new GraphConfig(args);

        GraphBuilder graphBuilder = new GraphBuilder(graphConfig.getConfig());

        // submit
        StreamApplication app = new StreamApplication(graphConfig.getGraphApplicationName(), UserConfig.empty(), graphBuilder.getGraph());
        context.submit(app);

        // clean resource
        context.close();
    }

    @Override
    public String toString() {
        return "Rule engine application for Gearpump";
    }
}
