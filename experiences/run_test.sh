#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

initSize=16384
range=32768
nbTest=3
benchmarkTime=5
warmingUpTime=2

for nbThread in 1 2 4 8 16 32 48 64 70 86 96
do
  perf stat -B -e cache-references,cache-misses ./test.sh -c CounterJUC -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -g $nbThread
#  perf stat -B -e cache-references,cache-misses ./test.sh -c CounterIncrementOnly -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -g $nbThread
#  perf stat -B -e cache-references,cache-misses ./test.sh -c LongAdder -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -g $nbThread
done

#perf stat -B -e cache-references,cache-misses ./test.sh -s Set -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range
#perf stat -B -e cache-references,cache-misses ./test.sh -s SegmentedSkipListSet -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range
#perf stat -B -e cache-references,cache-misses ./test.sh -s ConcurrentHashSet -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range
#perf stat -B -e cache-references,cache-misses ./test.sh -s SegmentedHashSet -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range
#perf stat -B -e cache-references,cache-misses ./test.sh -s ExtendedSegmentedHashSet -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range
#perf stat -B -e cache-references,cache-misses ./test.sh -s ExtendedShardedHashSet -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range
##perf stat -B -e cache-references,cache-misses ./test.sh -s ShardedHashSet -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range
##perf stat -B -e cache-references,cache-misses ./test.sh -s SegmentedTreeSet -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range
##perf stat -B -e cache-references,cache-misses ./test.sh -s ShardedTreeSet -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range
##
#perf stat -B -e cache-references,cache-misses ./test.sh -q Queue -t Microbenchmark -p -e -r "100 0 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -a -d $range
#perf stat -B -e cache-references,cache-misses ./test.sh -q QueueMASP -t Microbenchmark -p -e -r "100 0 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -a -d $range
##
#perf stat -B -e cache-references,cache-misses ./test.sh -m Map -t Microbenchmark -p -e -r "25 25 50" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -k
##perf stat -B -e cache-references,cache-misses ./test.sh -m ShardedHashMap -t Microbenchmark -p -e -r "25 25 50" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -k
#perf stat -B -e cache-references,cache-misses ./test.sh -m SegmentedHashMap -t Microbenchmark -p -e -r "25 25 50" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -k
#perf stat -B -e cache-references,cache-misses ./test.sh -m ExtendedSegmentedConcurrentHashMap -t Microbenchmark -p -e -r "25 25 50" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -k
#perf stat -B -e cache-references,cache-misses ./test.sh -m ConcurrentSkipListMap -t Microbenchmark -p -e -r "25 25 50" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -k
#perf stat -B -e cache-references,cache-misses ./test.sh -m SegmentedSkipListMap -t Microbenchmark -p -e -r "25 25 50" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -k
#perf stat -B -e cache-references,cache-misses ./test.sh -m ExtendedSegmentedSkipListMap -t Microbenchmark -p -e -r "25 25 50" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -k
##perf stat -B -e cache-references,cache-misses ./test.sh -m SegmentedTreeMap -t Microbenchmark -p -e -r "25 25 50" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -k
#

