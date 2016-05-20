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
package com.intel.ruleengine.gearpump.graph;

import io.gearpump.cluster.UserConfig;
import io.gearpump.partitioner.HashPartitioner;
import io.gearpump.partitioner.Partitioner;
import io.gearpump.streaming.javaapi.Graph;
import io.gearpump.streaming.javaapi.Processor;

public class GraphBuilder {
    private final Graph graph;

    private Partitioner hashPartitioner;
    private final GraphDefinition graphDefinition;

    public GraphBuilder(UserConfig config) {
        this.graph = new Graph();
        ParallelismDefinition parallelismDefinition = new ParallelismDefinition();
        ProcessorsBuilder processorsBuilder = new ProcessorsBuilder(config, parallelismDefinition);
        this.graphDefinition = new GraphDefinition(processorsBuilder);
        createPartitioners();
        buildGraph();
    }

    private void createPartitioners() {
        hashPartitioner = new HashPartitioner();
    }

    private Graph buildGraph() {
        createVertexes();
        createEdges();
        return graph;
    }

    public Graph getGraph() {
        return graph;
    }

    private void createVertexes() {
        for (Processor processor : graphDefinition.getDefinition().keySet()) {
            graph.addVertex(processor);
        }
    }

    private void createEdges() {
        for (Processor processor : graphDefinition.getDefinition().keySet()) {
            for (Processor childProcessor : graphDefinition.getDefinition().get(processor)) {
                graph.addEdge(processor, hashPartitioner, childProcessor);
            }
        }
    }

}
