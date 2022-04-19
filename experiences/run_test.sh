#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

mvn clean package -f ../java -DskipTests;

for ratio in 50; #95 80 50 20 5;
do
for type in 'Counter' 'DegradableCounter' 'FuzzyCounter'
#for type in 'Counter' 'DegradableCounter' 'ConcurrentLinkedQueue' 'MapQueue' 'ConcurrentSkipListSet' 'DegradableSet' 'ConcurrentHashMap' 'DegradableMap'
  do
    echo " "
    echo $type $ratio
    CLASSPATH=../java/target/*:../java/target/lib/* java -XX:+UseNUMA -XX:+UseG1GC eu.cloudbutton.dobj.Benchmark.Benchmark -type $type -ratios $ratio -nbTest 5 -time 10 -wTime 5 -s -p
  done
done
