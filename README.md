# IoT Analytics Gearpump Rule Engine

This is an implementation of Gearpump application, which allow you to run specific rules on data ingested into IoT Analytics
(such as temperature is higher than 22) and trigger an alert, if ingested data pass the conditions specified in rules.

It is a part of IoT Analytics solution and previous deployment of iotanalytics-dashboard and iotanalytics-backend is required.

In order to find out more about gearpump real-time big data streaming engine, please visit: http://gearpump.apache.org

Currently it supports rules consisting of:

* Basic conditions (e.g. there was an observation with value higher than 25)
* Timebased conditions (e.g. temperature higher than 30C for at least 5 minutes)
* Statistics based conditions (e.g. there are observations with value higher than average plus/minus 2 or 3 standard deviations )

connected using logical AND or OR operators.

## Requirements 

1. Java 1.8 or higher
1. Apache Maven 2.2.1 or higher
1. Python 2.7
1. Cloud Foundry CLI and Trusted Analytics Platform account (https://github.com/trustedanalytics)

## Deployment manual

#### On Trusted Analytics Platform (https://github.com/trustedanalytics)
Before installation, make sure that you are logged into Trusted Analytics Platform with command:
```
cf login
```

1. Create instances with specified name for each of required services from marketplace:

  * Kafka broker with name mykafka
  * Hbase broker with name myhbase
  * Zookeeper broker with name myzookeeper
  * Gearpump broker with name mygearpump

1. Create following user-provided services with properties filled with real values:

        cf cups dashboard-endpoint-ups -p "{\"host\":\"${ADDRESS}\"}"

    If you have deployed dashboard already, then you should have user and password set previously.
    This is only reminder that same service need to be attached to IoT Analytics Gearpump Rule Engine also.

        cf cups rule-engine-credentials-ups -p "{\"username\":\"${USER}\",\"password\":\"${PASSWORD}\"}"

    If you have deployed backend already, then you should kerberos credentials set previously.
    This is only reminder that same service need to be attached to IoT Analytics Gearpump Rule Engine also.

        cf cs kerberos shared kerberos-service

    If you have deployed backend already, then you should kafka topic properties set previously.
    This is only reminder that same service need to be attached to IoT Analytics Gearpump Rule Engine also.

        cf cups kafka-ups -p '{"topic":"example_topic_name","enabled":true,"partitions":1,"replication":1,"timeout_ms":10000}'

1. Execute ./cf-deploy.sh (in main repository catalog). It will download dependencies, compile project and push application to TAP
 with name gearpump-deployer. After startup gearpump-deployer application will submit IoT Analytics Gearpump Rule Engine into YARN cluster using TAP's gearpump-broker instance.
1. Check logs and wait for application start.
