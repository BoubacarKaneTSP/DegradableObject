#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

mvn clean package -f ../java -DskipTests;

echo "********** Test objet java concurrent **********"
CLASSPATH=../java/target/*:../java/target/lib/* java -XX:+UseNUMA -XX:+UseG1GC eu.cloudbutton.dobj.Benchmark.App -set ConcurrentSkipListSet -queue ConcurrentLinkedQueue -counter Counter -map ConcurrentHashMap -distribution 5 15 30 50 -nbTest 1 -time 1 -wTime 3 -p

echo "********** Test objet degradable **********"
#CLASSPATH=../java/target/*:../java/target/lib/* java -XX:+UseNUMA -XX:+UseG1GC eu.cloudbutton.dobj.Benchmark.App -set DegradableSet -queue MapQueue -counter DegradableCounter -map DegradableMap -distribution 5 15 30 50 -nbTest 5 -time 10 -wTime 5 -s -p
