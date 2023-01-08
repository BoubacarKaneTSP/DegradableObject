#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

nbTest=5
benchmarkTime=20
warmingUpTime=10
initNbUsers=1000000

perf stat -B -e cache-references,cache-misses ./test.sh -c CounterJUC -s ConcurrentSkipListSet -q ConcurrentLinkedQueue -m ConcurrentHashMap -t Retwis -r "5 15 30 50" -p -e -w $benchmarkTime -u $warmingUpTime -n $nbTest -b -h "JUC" -i $initNbUsers -k -j
#perf stat -B -e cache-references,cache-misses ./test.sh -c CounterIncrementOnly -s SetAddIntensive -q QueueMASP -m MapAddIntensive -t Retwis -r "5 15 30 50" -p -e -w $benchmarkTime -u $warmingUpTime -n $nbTest -b -h "Q_M_S_C" -i -i $initNbUsers -k

##for nbThread in 1 2 4 8 16 32 48
#for nbUser in 100 1000 10000 100000 1000000
##for nbUser in 100 1000 10000 100000 1000000
#do
#  echo
#	echo "Results for : $nbUser users"
##	echo "Results for : $nbThread"
#	echo
#
#
#done
