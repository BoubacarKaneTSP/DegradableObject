#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

nbTest=2
benchmarkTime=20
warmingUpTime=5
#nbUsersInit=1000
nbHashCode=1000000
nbOps=100000
ratio="0 15 30 55"

for nbUsersInit in 100 10000 1000000
do
perf stat -B -e cache-references,cache-misses ./test.sh -c Counter -s ConcurrentSkipListSet -q Queue -m Map -t Retwis -r "$ratio" -p -e -w $benchmarkTime -u $warmingUpTime -n $nbTest -h "JUC" -y $nbUsersInit -d $nbHashCode -i $nbOps -b
perf stat -B -e cache-references,cache-misses ./test.sh -c CounterIncrementOnly -s SegmentedSkipListSet -q QueueMASP -m ExtendedSegmentedConcurrentHashMap -t Retwis -r "$ratio" -p -e -w $benchmarkTime -u $warmingUpTime -n $nbTest -h "Q_M_S_C" -y $nbUsersInit -d $nbHashCode -i $nbOps -b
done


