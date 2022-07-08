#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

rm Map_*

#for nbThread in "1" "2" "3" "4"
for nbThread in "1" "10" "20" "30" "40"
do
  for nbInitialAdd in "1000" "5000" "10000" "50000" "100000" "200000" "300000" "400000" "500000" "600000" "700000" "800000" "900000" "1000000"
#  for nbInitialAdd in "10" "50" "100" "500"
  do
#    CLASSPATH=../java/target/*:../java/target/lib/* numactl -N 0 -m 0 java -XX:+UseNUMA -XX:+UseG1GC eu.cloudbutton.dobj.Benchmark.Benchmark -type Map -ratios "100 0 0" -nbThreads $nbThread -nbTest 1 -time 5 -wTime 1 -nbOps $nbInitialAdd -p -s -collisionKey >> "Map_${nbThread}_thread.txt"
    CLASSPATH=../java/target/*:../java/target/lib/* numactl -N 1 -m 0 java -XX:+UseNUMA -XX:+UseG1GC eu.cloudbutton.dobj.Benchmark.Benchmark -type Map -ratios "100 0 0" -nbThreads $nbThread -nbTest 3 -time 15 -wTime 5 -nbOps $nbInitialAdd -p -s -collisionKey >> "Map_${nbThread}_thread.txt"
  done
done
