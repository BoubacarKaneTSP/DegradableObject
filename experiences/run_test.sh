#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

mvn clean package -f ../java -DskipTests;

for ratio in 100; # 80 50 20 5;
do
  for type in 'Counter' 'Set' 'List' 'LinkedList' 'DegradableCounter' 'DegradableSet' 'DegradableList' 'DegradableLinkedList' 'CounterSnapshot' 'SetSnapshot' 'ListSnapshot' 'LinkedListSnapshot' 'CounterSnapshotV2' 'SetSnapshotV2' 'ListSnapshotV2' 'LinkedListSnapshotV2'
#  for type in 'DegradableCounter'
    do
    echo $type $ratio
    CLASSPATH=../java/target/*:../java/target/lib/* java eu.cloudbutton.dobj.Benchmark -type $type -ratios $ratio -nbTest 1 -time 30 -wTime 10 # > "results_${type}_ratio_write_${ratio}_SR.txt"
  done
done


