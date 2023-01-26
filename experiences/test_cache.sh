#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

nbTest=5
benchmarkTime=20
warmingUpTime=10
initNbUsers=100
nbHashCode=100

perf stat -B -e cache-references,cache-misses ./test.sh -c CounterJUC -s ConcurrentSkipListSet -q ConcurrentLinkedQueue -m ConcurrentHashMap -t Retwis -r "5 15 30 50" -p -e -w $benchmarkTime -u $warmingUpTime -n $nbTest -h "JUC" -i $initNbUsers -d $nbHashCode -j
perf stat -B -e cache-references,cache-misses ./test.sh -c CounterIncrementOnly -s SetAddIntensive -q QueueMASP -m MapAddIntensive -t Retwis -r "5 15 30 50" -p -e -w $benchmarkTime -u $warmingUpTime -n $nbTest -b -h "Q_M_S_C" -i $initNbUsers -d $nbHashCode -j

