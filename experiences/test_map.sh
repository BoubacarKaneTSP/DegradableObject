#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

for nbThread in 2 40
do
  for nbInitialAdd in 1000 5000 10000 50000 100000 200000 300000 400000 500000 600000 700000 800000 900000 1000000
  do
    CLASSPATH=../java/target/*:../java/target/lib/* numactl -N 1 -m 0 java -XX:+UseNUMA -XX:+UseG1GC eu.cloudbutton.dobj.Benchmark.Benchmark -type Map -ratios "100 0 0" -nbThreads $nbThread -nbTest 5 -time 20 -wTime 10 -nbOps $nbInitialAdd -p -s -asymmetric -collisionKey >> "Map_$nbThread_thread.txt"
  done
done