#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

initSize=16384
range=32768
nbTest=5
benchmarkTime=30
warmingUpTime=10
#
#perf stat -B -e cache-references,cache-misses ./test.sh -s Set -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range
#perf stat -B -e cache-references,cache-misses ./test.sh -s SegmentedSkipListSet -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range
#perf stat -B -e cache-references,cache-misses ./test.sh -s ConcurrentHashSet -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range
#perf stat -B -e cache-references,cache-misses ./test.sh -s SegmentedHashSet -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range
#perf stat -B -e cache-references,cache-misses ./test.sh -s ShardedHashSet -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range
#perf stat -B -e cache-references,cache-misses ./test.sh -s SegmentedTreeSet -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range
#perf stat -B -e cache-references,cache-misses ./test.sh -s ShardedTreeSet -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range
#
#perf stat -B -e cache-references,cache-misses ./test.sh -q Queue -t Microbenchmark -p -e -r "100 0 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -a -d $range
#perf stat -B -e cache-references,cache-misses ./test.sh -q QueueMASP -t Microbenchmark -p -e -r "100 0 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -a -d $range
#
#perf stat -B -e cache-references,cache-misses ./test.sh -m Map -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -k
#perf stat -B -e cache-references,cache-misses ./test.sh -m ShardedHashMap -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -k
#perf stat -B -e cache-references,cache-misses ./test.sh -m SegmentedHashMap -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -k
perf stat -B -e cache-references,cache-misses ./test.sh -m ConcurrentSkipListMap -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -k
perf stat -B -e cache-references,cache-misses ./test.sh -m SegmentedSkipListMap -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -k
perf stat -B -e cache-references,cache-misses ./test.sh -m SegmentedTreeMap -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -k

#perf stat -B -e cache-references,cache-misses ./test.sh -c Counter -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range
#perf stat -B -e cache-references,cache-misses ./test.sh -c CounterIncrementOnly -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range
#perf stat -B -e cache-references,cache-misses ./test.sh -c LongAdder -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range
