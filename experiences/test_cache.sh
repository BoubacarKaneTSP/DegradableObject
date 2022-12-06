#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

for nbThread in 1 2 4 8 16 32 48
do
	echo $nbThread
	echo

  perf stat -B -e cache-references,cache-misses ./test.sh -c CounterJUC -s ConcurrentSkipListSet -q ConcurrentLinkedQueue -m ConcurrentHashMap -t Retwis -p -e -w 30 -u 10 -n 3 -i 100 -b -h "JUC" -g $nbThread
	perf stat -B -e cache-references,cache-misses ./test.sh -c CounterIncrementOnly -s ConcurrentSkipListSet -q QueueMASP -m MapAddIntensive -t Retwis -p -e -w 30 -u 10 -n 3 -i 100 -b -h "Q_M_S_C" -g $nbThread

done
