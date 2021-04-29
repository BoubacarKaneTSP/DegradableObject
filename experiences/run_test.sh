#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

mvn clean package -f ../java -DskipTests;

for ratio in 100 80 50 20 0;
do
  for type in 'Counter' 'DegradableCounter' 'CounterSnapshot' 'List' 'DegradableList' 'ListSnapshot' 'Set' 'DegradableSet' 'SetSnapshot'
  do
    echo $type $ratio
    if [ $type = 'Counter'  ] || [ $type = 'DegradableCounter' ] ; then
           CLASSPATH=../java/target/*:../java/target/lib/* java  eu.cloudbutton.dobj.Benchmark -type $type -ratios $ratio -nbTest 5 -nbOps 15000000000 > "results_${type}_ratio_write_${ratio}.txt"
    elif [ $type = 'CounterSnapshot' ] || [ $type = 'SetSnapshot' ] || [ $type = 'ListSnapshot' ]; then
     CLASSPATH=../java/target/*:../java/target/lib/* java eu.cloudbutton.dobj.Benchmark -type $type -ratios $ratio -nbTest 5 -nbOps 300000 > "results_${type}_ratio_write_${ratio}.txt"
    else
       CLASSPATH=../java/target/*:../java/target/lib/* java  eu.cloudbutton.dobj.Benchmark -type $type -ratios $ratio -nbTest 5 -nbOps 3000000 > "results_${type}_ratio_write_${ratio}.txt"
    fi
  done
done


