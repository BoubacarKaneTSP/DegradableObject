#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

mvn clean package -f ../java -DskipTests;
CLASSPATH=../java/target/*:../java/target/lib/* java -XX:+UseNUMA -XX:+UseG1GC eu.cloudbutton.dobj.App -set DegradableSet -list DegradableList -counter DegradableCounter -ratios 5 15 30 50 -nbTest 1 -time 10 -wTime 5 #> "results_${type}_ratio_write_${ratio}_SR.txt"
CLASSPATH=../java/target/*:../java/target/lib/* java -XX:+UseNUMA -XX:+UseG1GC eu.cloudbutton.dobj.App -set Set -list List -counter Counter -ratios 5 15 30 50 -nbTest 1 -time 10 -wTime 5 #> "results_${type}_ratio_write_${ratio}_SR.txt"
