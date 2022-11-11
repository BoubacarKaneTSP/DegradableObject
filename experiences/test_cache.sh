#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

for nbThread in 1 2 4 8 16 32 48
do
	echo $nbThread
	echo

	perf stat -B -e cache-references,cache-misses ./test.sh -c CounterJUC -s ConcurrentSkipListSet -q QueueMASP -m ConcurrentHashMap -t Retwis -p -e -w 20 -u 5 -n 1 -b -h "JUC" -i 100000000 -g $nbThread
done
