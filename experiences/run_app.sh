#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

mvn clean package -f ../java -DskipTests;

echo "********** Test objet java concurrent **********"
CLASSPATH=../java/target/*:../java/target/lib/* numactl -N 0 -m 0 java -XX:+UseNUMA -XX:+UseG1GC eu.cloudbutton.dobj.Benchmark.App -set ConcurrentSkipListSet -queue DegradableQueue -counter Counter -map ConcurrentHashMap -distribution 5 15 30 50 -nbTest 1 -time 5 -wTime 2 -p -s

echo "********** Test objet degradable **********"
#CLASSPATH=../java/target/*:../java/target/lib/* numactl -N 0 -m 0 java -XX:+UseNUMA -XX:+UseG1GC eu.cloudbutton.dobj.Benchmark.App -set DegradableSet -queue DegradableQueue -counter FuzzyCounter -map DegradableMap -distribution 5 15 30 50 -nbTest 1 -time 5 -wTime 2 -p -s
