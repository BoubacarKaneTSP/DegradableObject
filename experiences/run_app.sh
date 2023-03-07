#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

mvn clean package -f ../java -DskipTests;

typeCounter=""
typeSet=""
typeQueue=""
typeList=""
typeMap=""
typeTest=""
ratio="100 0 0"
distribution="0 10 35 55"
print=""
save=""
completionTime=""
nbUserInit=""
workloadTime=""
warmingUpTime=""
nbTest=1
type=""
printFail=""
asymmetric=""
collisionKey=""
quickTest=""
nbInitialAdd=""
breakdown=""
tag=""
nbThreads=""
nbItemsPerThread=""
computeGCInfo=false

echo "********** Test objet java concurrent **********"

if [[ $computeGCInfo == true ]]
 then
  CLASSPATH=../java/target/*:../java/target/lib/* numactl -N 0 -m 0 java -Xlog:gc -Xms5g -Xmx20g -XX:+UseNUMA -XX:+UseG1GC -XX:-RestrictContended -ea eu.cloudbutton.dobj.benchmark.Retwis -set $typeSet -queue $typeQueue -counter $typeCounter -map $typeMap -distribution $ratio -nbTest $nbTest $nbThreads $workloadTime $warmingUpTime $nbInitialAdd $completionTime $nbUserInit $print $save $breakdown $quickTest $collisionKey $nbItemsPerThread -tag $tag -gcinfo | egrep "nbThread|benchmarkAvgTime|Start benchmark|End benchmark|G1 Evacuation Pause" > "$tag"_gcinfo.log
  python3 analyse_gc.py $tag $nbTest $nbUserInit
  else
  CLASSPATH=../java/target/*:../java/target/lib/* numactl -N 0 -m 0 java -Xms5g -Xmx20g -XX:+UseNUMA -XX:+UseG1GC -XX:-RestrictContended -ea eu.cloudbutton.dobj.benchmark.Retwis -set $typeSet -queue $typeQueue -counter $typeCounter -map $typeMap -distribution $ratio -nbTest $nbTest $nbThreads $workloadTime $warmingUpTime $nbInitialAdd $completionTime $nbUserInit $print $save $breakdown $quickTest $collisionKey $nbItemsPerThread -tag $tag
  fi
echo "********** Test objet degradable **********"
#CLASSPATH=../java/target/*:../java/target/lib/* numactl -N 0 -m 0 java -XX:+UseNUMA -XX:+UseG1GC eu.cloudbutton.dobj.Benchmark.App -set DegradableSet -queue DegradableQueue -counter FuzzyCounter -map DegradableMap -distribution 5 15 30 50 -nbTest 1 -time 5 -wTime 2 -p -s
