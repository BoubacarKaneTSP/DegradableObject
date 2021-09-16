#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

mvn clean package -f ../java -DskipTests;

for ratio in 100; #95 80 50 20 5;
do
#  for type in 'LinkedList' 'DegradableLinkedList' 'LinkedListSnapshot' 'LinkedListSnapshotSRMW'
#  for type in 'Counter' 'DegradableCounter' 'Set' 'DegradableSet' 'List' 'DegradableList' 'LinkedList' 'DegradableLinkedList' 'CounterSnapshot' 'SetSnapshot' 'ListSnapshot' 'LinkedListSnapshot' 'CounterSnapshotSRMW' 'SetSnapshotSRMW' 'ListSnapshotSRMW' 'LinkedListSnapshotSRMW'
#for type in 'Counter' 'DegradableCounter' 'Set' 'DegradableSet' 'List' 'DegradableList' 'LinkedList' 'DegradableLinkedList'
for type in 'DegradableQueue' 'Counter' 'DegradableCounter' 'List' 'DegradableList' 'Set' 'DegradableSet'
  do
    echo $type $ratio
    CLASSPATH=../java/target/*:../java/target/lib/* java -XX:+UseNUMA -XX:+UseG1GC eu.cloudbutton.dobj.Benchmark -type $type -ratios $ratio -nbTest 1 -time 10 -wTime 5 > "results_${type}_ratio_write_${ratio}_SR2.txt"
  done
done