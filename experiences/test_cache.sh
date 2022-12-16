#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

#for nbThread in 1 2 4 8 16 32 48
for nbUser in 100 1000 10000 100000 1000000
do
  echo
	echo "Results for : $nbUser users"
#	echo "Results for : $nbThread"
	echo

  perf stat -B -M L1MPKI,L2MPKI,L3MPKI -e cache-references,cache-misses ./test.sh -c CounterJUC -s ConcurrentSkipListSet -q ConcurrentLinkedQueue -m ConcurrentHashMap -t Retwis -p -e -w 30 -u 10 -n 3 -i $nbUser -b -h "JUC"
	perf stat -B -M L1MPKI,L2MPKI,L3MPKI -e cache-references,cache-misses ./test.sh -c CounterIncrementOnly -s SetAddIntensive -q QueueMASP -m MapAddIntensive -t Retwis -p -e -w 30 -u 10 -n 3 -i $nbUser -b -h "Q_M_S_C"
done
