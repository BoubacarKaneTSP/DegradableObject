#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

nbTest=5
benchmarkTime=20
warmingUpTime=5
initNbUsers=1000000
nbHashCode=10000000

perf stat -B -e cache-references,cache-misses ./test.sh -c Counter -s Set -q Queue -m Map -t Retwis -r "5 15 30 50" -p -e -w $benchmarkTime -u $warmingUpTime -n $nbTest -h "JUC" -i $initNbUsers -d $nbHashCode -b
perf stat -B -e cache-references,cache-misses ./test.sh -c CounterIncrementOnly -s SegmentedSkipListSet -q QueueMASP -m ShardedHashMap -t Retwis -r "5 15 30 50" -p -e -w $benchmarkTime -u $warmingUpTime -n $nbTest -h "Q_M_S_C" -i $initNbUsers -d $nbHashCode -b

