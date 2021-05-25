#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

mvn clean package -f ../java -DskipTests;

for ratio in 95 80 50 20 0;
do
  for type in 'Counter' 'DegradableCounter' 'CounterSnapshot' 'List' 'DegradableList' 'ListSnapshot' 'Set' 'DegradableSet' 'SetSnapshot' 'SecondDegradableList' 'ThirdDegradableList'
  do
    echo $type $ratio
    if [ $type = 'Counter' ]; then
        nbOperation=2800000000
    elif [ $type = 'DegradableCounter' ]; then
        nbOperation=1900000000
    elif [ $type = 'CounterSnapshot' ]; then
        nbOperation=50000000
    elif [ $type = 'List' ]; then
        nbOperation=75700000
    elif [ $type = 'DegradableList' ]; then
        nbOperation=69400000
    elif [ $type = 'ListSnapshot' ]; then
        nbOperation=12500000
    elif [ $type = 'Set' ]; then
        nbOperation=11900000
    elif [ $type = 'DegradableSet' ]; then
        nbOperation=12500000
    elif [ $type = 'SetSnapshot' ]; then
        nbOperation=8300000
    elif [ $type = 'SecondDegradableList' ]; then
        nbOperation=62500000
    elif [ $type = 'ThirdDegradableList' ]; then
        nbOperation=17800000
    fi



    if [ $type = 'Counter'  ] || [ $type = 'DegradableCounter' ] ; then
        CLASSPATH=../java/target/*:../java/target/lib/* java  eu.cloudbutton.dobj.Benchmark -type $type -ratios $ratio -nbTest 3 -time 60 -nbOps $nbOperation > "results_${type}_ratio_write_${ratio}.txt"
    elif [ $type = 'CounterSnapshot' ] || [ $type = 'SetSnapshot' ] || [ $type = 'ListSnapshot' ]; then
    	CLASSPATH=../java/target/*:../java/target/lib/* java eu.cloudbutton.dobj.Benchmark -type $type -ratios $ratio -nbTest 3 -time 60 -nbOps $nbOperation > "results_${type}_ratio_write_${ratio}.txt"
    else
    	CLASSPATH=../java/target/*:../java/target/lib/* java  eu.cloudbutton.dobj.Benchmark -type $type -ratios $ratio -nbTest 3 -time 60 -nbOps $nbOperation > "results_${type}_ratio_write_${ratio}.txt"
    fi
  done
done


