#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

nbTest=3
benchmarkTime=20
warmingUpTime=5
nbUsersInit=1000
nbHashCode=10000
nbOps=100000000

perf stat -B -e cache-references,cache-misses ./test.sh -c Counter -s ConcurrentHashSet -q Queue -m Map -t Retwis -r "5 15 30 50" -p -e -w $benchmarkTime -u $warmingUpTime -n $nbTest -h "JUC" -y $nbUsersInit -d $nbHashCode -i $nbOps -b -z
perf stat -B -e cache-references,cache-misses ./test.sh -c CounterIncrementOnly -s ShardedHashSet -q QueueMASP -m ExtendedSegmentedConcurrentHashMap -t Retwis -r "5 15 30 50" -p -e -w $benchmarkTime -u $warmingUpTime -n $nbTest -h "Q_M_S_C" -y $nbUsersInit -d $nbHashCode -i $nbOps -b -z

