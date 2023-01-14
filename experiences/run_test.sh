#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

initSize=1
range=2048
nbTest=5
benchmarkTime=20
warmingUpTime=10

perf stat -B -e cache-references,cache-misses ./test.sh -s AtomicReference -t Microbenchmark -p -r "0 0 100" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -j
perf stat -B -e cache-references,cache-misses ./test.sh -s AtomicWriteOnceReference -t Microbenchmark -p -r "0 0 100" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -j
