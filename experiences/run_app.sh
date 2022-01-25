#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

mvn clean package -f ../java -DskipTests;

echo "********** Test objet java concurrent **********"
#CLASSPATH=../java/target/*:../java/target/lib/* java -XX:+UseNUMA -XX:+UseG1GC eu.cloudbutton.dobj.App -set Set -list List -counter Counter -ratios 5 15 30 50 -nbTest 5 -time 10 -wTime 5 -alphaInit 1.5 -alphaMin 1.2 -alphaStep 0.05 -s -p

echo "********** Test objet degradable **********"
CLASSPATH=../java/target/*:../java/target/lib/* java -XX:+UseNUMA -XX:+UseG1GC eu.cloudbutton.dobj.App -set Set -list MapQueue -counter DegradableCounter -ratios 5 15 30 50 -nbTest 5 -time 10 -wTime 5 -alphaInit 1.315 -alphaMin 1.315 -alphaStep 0.05 -s -p
