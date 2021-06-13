#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

mvn clean package -f ../java -DskipTests;

for ratio in 95 80 50 20 5;
do
  for type in 'Counter' 'Set' 'List' 'DegradableCounter' 'DegradableSet' 'DegradableList' 'CounterSnapshot' 'SetSnapshot' 'ListSnapshot' 'SecondDegradableList' 'ThirdDegradableList'
  do
    echo $type $ratio
    CLASSPATH=../java/target/*:../java/target/lib/* java eu.cloudbutton.dobj.Benchmark -type $type -ratios $ratio -nbTest 1 -time 60 -wTime 25 > "results_${type}_ratio_write_${ratio}_single_reader.txt"
  done
done


