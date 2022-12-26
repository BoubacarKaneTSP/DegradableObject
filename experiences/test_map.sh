#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT


perf stat -B -e cache-references,cache-misses ./test.sh -s Set -t Microbenchmark -p -e -r "50 50 0" -w 10 -u 5 -n 1 -i 1000000 -d 32000
perf stat -B -e cache-references,cache-misses ./test.sh -s SegmentedSkipListSet -t Microbenchmark -p -e -r "50 50 0" -w 10 -u 5 -n 1 -i 1000000 -d 32000
perf stat -B -e cache-references,cache-misses ./test.sh -s ShardedTreeSet -t Microbenchmark -p -e -r "50 50 0" -w 10 -u 5 -n 1 -i 1000000 -d 32000
perf stat -B -e cache-references,cache-misses ./test.sh -s ShardedSet -t Microbenchmark -p -e -r "50 50 0" -w 10 -u 5 -n 1 -i 1000000 -d 32000
#
#perf stat -B -e cache-references,cache-misses ./test.sh -s Queue -t Microbenchmark -p -e -r "100 0 0" -w 10 -u 5 -n 2 -i 1000000 -a
#perf stat -B -e cache-references,cache-misses ./test.sh -s QueueMASP -t Microbenchmark -p -e -r "100 0 0" -w 10 -u 5 -n 2 -i 1000000 -a

#perf stat -B -e cache-references,cache-misses ./test.sh -s Map -t Microbenchmark -p -e -r "50 50 0" -w 10 -u 5 -n 2 -i 1000000 -k
#perf stat -B -e cache-references,cache-misses ./test.sh -s ShardedHashMap -t Microbenchmark -p -e -r "50 50 0" -w 10 -u 5 -n 2 -i 1000000 -k
#perf stat -B -e cache-references,cache-misses ./test.sh -s SegmentedHashMap -t Microbenchmark -p -e -r "50 50 0" -w 10 -u 5 -n 2 -i 1000000 -k

#perf stat -B -e cache-references,cache-misses ./test.sh -c CounterJUC -s ConcurrentSkipListSet -q ConcurrentLinkedQueue -m ConcurrentHashMap -t Retwis -p -e -w 30 -u 10 -n 3 -i 1000 -b -h "JUC"
##./test.sh -c CounterJUC -s ConcurrentSkipListSet -q ConcurrentLinkedQueue -m MapAddIntensive -t Retwis -p -e -w 15 -u 5 -n 1 -b -h "M"
##./test.sh -c CounterJUC -s SetAddIntensive -q ConcurrentLinkedQueue -m MapAddIntensive -t Retwis -p -e -w 15 -u 5 -n 1 -b -h "M_S"
##./test.sh -c CounterJUC -s SetAddIntensive -q QueueMASP -m MapAddIntensive -t Retwis -p -e -w 15 -u 5 -n 1 -b -h "Q_M_S"
#perf stat -B -e cache-references,cache-misses ./test.sh -c CounterIncrementOnly -s ConcurrentSkipListSet -q QueueMASP -m MapAddIntensive -t Retwis -p -e -w 30 -u 10 -n 3 -i 1000 -b -h "Q_M_S_C"
#
##./test.sh -c CounterJUC -s ConcurrentSkipListSet -q ConcurrentLinkedQueue -m ConcurrentHashMap -t Retwis -p -e -w 15 -u 5 -n 5 -z -h "JUC_CompletionTime"
##./test.sh -c CounterJUC -s ConcurrentSkipListSet -q QueueMASP -m ConcurrentHashMap -t Retwis -p -e -w 15 -u 5 -n 5 -z -h "Q_CompletionTime"
##./test.sh -c CounterJUC -s ConcurrentSkipListSet -q QueueMASP -m MapAddIntensive -t Retwis -p -e  -w 15 -u 5 -n 5 -z -h "Q_M_CompletionTime"
##./test.sh -c CounterJUC -s SetAddIntensive -q QueueMASP -m MapAddIntensive -t Retwis -p -e -w 15 -u 5 -n 5 -z -h "Q_M_S_CompletionTime"
##./test.sh -c CounterIncrementOnly -s SetAddIntensive -q QueueMASP -m MapAddIntensive -t Retwis -p -e -w 15 -u 5 -n 5 -z -h "Q_M_S_C_CompletionTime"
