#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

nbTest=5
benchmarkTime=300
warmingUpTime=60
#nbUsersInit=1000
nbHashCode=1000000
nbOps=100000
ratio="5 15 30 50"

for nbUsersInit in 100 10000 1000000
do
  for nbThread in 1 2 4 8 16 32 48
  do
    perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions -o perf.log ./test.sh -c Counter -s ConcurrentHashSet -q Queue -m Map -t Retwis -r "$ratio" -p -e -w $benchmarkTime -u $warmingUpTime -n $nbTest -h "JUC" -y $nbUsersInit -d $nbUsersInit -i $nbOps -b -j -g $nbThread
    python3 analyse_perf.py perf.log "JUC" $nbThread $nbUsersInit
    perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions -o perf.log ./test.sh -c CounterIncrementOnly -s ConcurrentHashSet -q QueueMASP -m ExtendedSegmentedConcurrentHashMap -t Retwis -r "$ratio" -p -e -w $benchmarkTime -u $warmingUpTime -n $nbTest -h "Q_M_S_C" -y $nbUsersInit -d $nbUsersInit -i $nbOps -b -j -g $nbThread
    python3 analyse_perf.py perf.log "Q_M_S_C" $nbThread $nbUsersInit
  done
done


