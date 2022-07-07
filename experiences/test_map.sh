#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

for nbInitialAdd in 10 100 1000 10000 100000 1000000 10000000
do
CLASSPATH=../java/target/*:../java/target/lib/* numactl -N 1 -m 0 java -XX:+UseNUMA -XX:+UseG1GC eu.cloudbutton.dobj.Benchmark.Benchmark -type Map -ratios "100 0 0" -nbThreads 1 -nbTest 5 -time 20 -wTime 10 -nbOps $nbInitialAdd -p -s -asymmetric -collisionKey >> Map_1_thread.txt
done
