#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

mvn clean package -f ../java -DskipTests;

for type in 'Counter' 'DegradableCounter' 'CounterSnapshot' 'List' 'DegradableList' 'ListSnapshot' 'Set' 'DegradableSet' 'SetSnapshot'
do
  echo $type
  if [ $type = 'Counter'  ] || [ $type = 'DegradableCounter' ] || [ $type = 'CounterSnapshot' ]; then
         CLASSPATH=../java/target/*:../java/target/lib/* java  eu.cloudbutton.dobj.Benchmark -type $type -ratios 100 -nbTest 5 -nbOps 1000000000 > "results_${type}.txt"
  elif [ $type = 'CounterSnapshot' ] || [ $type = 'SetSnapshot' ] || [ $type = 'ListSnapshot' ]; then
	 CLASSPATH=../java/target/*:../java/target/lib/* java eu.cloudbutton.dobj.Benchmark -type $type -ratios 100 -nbTest 5 -nbOps 500000 > "results_${type}.txt"
  else
     CLASSPATH=../java/target/*:../java/target/lib/* java  eu.cloudbutton.dobj.Benchmark -type $type -ratios 100 -nbTest 5 -nbOps 3000000 > "results_${type}.txt"
  fi
done
