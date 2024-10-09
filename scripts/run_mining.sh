#!/bin/bash

repos=(
    "https://github.com/apache/ignite"
    "https://github.com/apache/hadoop"
    "https://github.com/apache/cassandra"
    "https://github.com/apache/dubbo"
    "https://github.com/apache/kafka"
    "https://github.com/apache/incubator_seata"
    "https://github.com/apache/flink"
    "https://github.com/apache/skywalking"
    "https://github.com/apache/rocketmq"
    "https://github.com/apache/shardingsphere"
    "https://github.com/apache/pulsar"
    "https://github.com/apache/druid"
    "https://github.com/apache/dolphinscheduler"
    "https://github.com/apache/doris"
    "https://github.com/apache/zookeeper"
    "https://github.com/apache/shenyu"
    "https://github.com/apache/jmeter"
    "https://github.com/apache/shardingsphere-elasticjob"
    "https://github.com/apache/seatunnel"
    "https://github.com/apache/beam"
    "https://github.com/apache/tomcat"
    "https://github.com/apache/storm"
    "https://github.com/apache/zeppelin"
    "https://github.com/apache/iceberg"
    "https://github.com/apache/incubator-kie-drools"
    "https://github.com/apache/flink-cdc"
    "https://github.com/apache/iotdb"
    "https://github.com/apache/hertzbeat"
    "https://github.com/apache/camel"
    "https://github.com/apache/hive"
    "https://github.com/apache/pinot"
    "https://github.com/apache/hudi"
    "https://github.com/apache/hbase"
    "https://github.com/apache/groovy"
    "https://github.com/apache/nifi"
    "https://github.com/apache/rocketmq-externals"
    "https://github.com/apache/calcite"
    "https://github.com/apache/shiro"
    "https://github.com/apache/maven"
    "https://github.com/apache/kylin"
    "https://github.com/apache/logging-log4j2"
    "https://github.com/apache/incubator-kie-optaplanner"
    "https://github.com/apache/linkis"
    "https://github.com/apache/curator"
    "https://github.com/apache/fury"
    "https://github.com/apache/avro"
    "https://github.com/apache/maven-mvnd"
    "https://github.com/apache/nutch"
    "https://github.com/apache/commons-lang"
    "https://github.com/apache/netbeans"
    "https://github.com/apache/pdfbox"
)

classes=(
    "ConcurrentLinkedQueue"
    "ConcurrentHashMap"
    "AtomicReference"
    "ConcurrentSkipListSet"
)

class_args=""
for clazz in "${classes[@]}"
do
    class_args+="-c ${clazz} "
done

for repo in "${repos[@]}"
do
    echo "Analyzing repository: ${repo}"
    python3 mining.py -r "${repo}" ${class_args}
done
