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

package com.intel.ruleengine.gearpump.tasks;

import io.gearpump.cluster.UserConfig;
import io.gearpump.cluster.client.ClientContext;
import io.gearpump.streaming.javaapi.Processor;
import io.gearpump.streaming.kafka.KafkaSource;
import io.gearpump.streaming.kafka.KafkaStorageFactory;
import io.gearpump.streaming.kafka.lib.KafkaSourceConfig;
import kafka.api.OffsetRequest;

import java.util.Properties;

public class KafkaSourceProcessor {

    public static final String KAFKA_TOPIC_PROPERTY = "KAFKA_TOPIC";
    public static final String KAFKA_URI_PROPERTY = "KAFKA_URI";
    public static final String KAFKA_ZOOKEEPER_PROPERTY = "KAFKA_URI_ZOOKEEPER";

    private static final String NAME = "KafkaSource";

    private final KafkaSource kafkaSource;
    private final ClientContext context;

    public KafkaSourceProcessor(UserConfig userConfig) {
        String topic = userConfig.getString(KAFKA_TOPIC_PROPERTY).get();
        String zookeeperQuorum = userConfig.getString(KAFKA_ZOOKEEPER_PROPERTY).get();
        String serverUri = userConfig.getString(KAFKA_URI_PROPERTY).get();

        Properties zookeeperProperties = new Properties();
        zookeeperProperties.setProperty("zookeeper.connect", zookeeperQuorum);
        zookeeperProperties.setProperty("group.id", "gearpump");
        // todo what is the default storage on TAP?
        zookeeperProperties.setProperty("offsets.storage", "kafka");

        KafkaSourceConfig sourceConfig = new KafkaSourceConfig(zookeeperProperties)
                .withConsumerStartOffset(OffsetRequest.LatestTime());

        Properties kafkaProperties = new Properties();
        kafkaProperties.setProperty("bootstrap.servers", serverUri);

        KafkaStorageFactory offsetStorageFactory = new KafkaStorageFactory(sourceConfig.consumerProps(), kafkaProperties);
        kafkaSource = new KafkaSource(topic, sourceConfig.consumerProps(), offsetStorageFactory);
        context = ClientContext.apply();
    }

    public Processor getKafkaSourceProcessor(int parallelProcessorNumber) {
        return Processor.source(kafkaSource, parallelProcessorNumber, NAME, UserConfig.empty(), context.system());
    }
}
