#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

initSize=16384
range=32768
nbTest=5
benchmarkTime=60
warmingUpTime=30

#for nbThread in 80
#for nbThread in 1 2 4 8 16 32 48 96 160
for nbThread in 1 40 80 120 160
#for nbThread in 1 80
do
#  perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions,l1d_pend_miss.pending_cycles_any,l2_rqsts.all_demand_miss,cycle_activity.stalls_total -o perf.log ./test.sh -c AtomicReference -t Microbenchmark -p -e -r "0 0 100" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -g $nbThread
#  python3 analyse_perf.py perf.log "false" "AtomicReference" $nbThread ""
#
#  perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions,l1d_pend_miss.pending_cycles_any,l2_rqsts.all_demand_miss,cycle_activity.stalls_total -o perf.log ./test.sh -c AtomicWriteOnceReference -t Microbenchmark -p -e -r "0 0 100" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -g $nbThread
#  python3 analyse_perf.py perf.log "false" "AtomicWriteOnceReference" $nbThread ""

#
#  perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions,l1d_pend_miss.pending_cycles_any,l2_rqsts.all_demand_miss,cycle_activity.stalls_total -o perf.log ./test.sh -c CounterJUC -t Microbenchmark -p -e -r "100 0 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -g $nbThread
#  python3 analyse_perf.py perf.log "false" "CounterJUC" $nbThread ""
#
#  perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions,l1d_pend_miss.pending_cycles_any,l2_rqsts.all_demand_miss,cycle_activity.stalls_total -o perf.log ./test.sh -c CounterIncrementOnly -t Microbenchmark -p -e -r "100 0 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -g $nbThread
#  python3 analyse_perf.py perf.log "false" "CounterIncrementOnly" $nbThread ""
#
#  perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions,l1d_pend_miss.pending_cycles_any,l2_rqsts.all_demand_miss,cycle_activity.stalls_total -o perf.log ./test.sh -c LongAdder -t Microbenchmark -p -e -r "100 0 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -g $nbThread
#  python3 analyse_perf.py perf.log "false" "LongAdder" $nbThread ""
#
#  perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions,l1d_pend_miss.pending_cycles_any,l2_rqsts.all_demand_miss,cycle_activity.stalls_total -o perf.log ./test.sh -s Set -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -g $nbThread
#  python3 analyse_perf.py perf.log "false" "ConcurrentSkipListSet" $nbThread ""
#
#  perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions,l1d_pend_miss.pending_cycles_any,l2_rqsts.all_demand_miss,cycle_activity.stalls_total -o perf.log ./test.sh -s ConcurrentHashSet -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -g $nbThread
#  python3 analyse_perf.py perf.log "false" "ConcurrentHashSet" $nbThread ""

#  perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions,l1d_pend_miss.pending_cycles_any,l2_rqsts.all_demand_miss,cycle_activity.stalls_total -o perf.log ./test.sh -s SegmentedHashSet -t Microbenchmark -p -e -r "0 0 100" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -g $nbThread
#  python3 analyse_perf.py perf.log "false" "SegmentedHashSet" $nbThread ""

#  perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions,l1d_pend_miss.pending_cycles_any,l2_rqsts.all_demand_miss,cycle_activity.stalls_total -o perf.log ./test.sh -s ExtendedSegmentedSkipListMap -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -g $nbThread
#  python3 analyse_perf.py perf.log "false" "ExtendedSegmentedHashSet" $nbThread ""

#  perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions,l1d_pend_miss.pending_cycles_any,l2_rqsts.all_demand_miss,cycle_activity.stalls_total -o perf.log ./test.sh -q Queue -t Microbenchmark -p -e -r "100 0 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -a -d $range -g $nbThread
#  python3 analyse_perf.py perf.log "false" "ConcurrentLinkedQueue" $nbThread ""

#  perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions,l1d_pend_miss.pending_cycles_any,l2_rqsts.all_demand_miss,cycle_activity.stalls_total -o perf.log ./test.sh -q QueueMASP -t Microbenchmark -p -e -r "100 0 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -a -d $range -g $nbThread
#  python3 analyse_perf.py perf.log "false" "QueueMASP" $nbThread ""

##
#  perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions,l1d_pend_miss.pending_cycles_any,l2_rqsts.all_demand_miss,cycle_activity.stalls_total -o perf.log ./test.sh -m Map -t Microbenchmark -p -e -r " 38 37 25" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -g $nbThread
#  python3 analyse_perf.py perf.log "false" "ConcurrentHashMap" $nbThread ""
#
#  perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions,l1d_pend_miss.pending_cycles_any,l2_rqsts.all_demand_miss,cycle_activity.stalls_total -o perf.log ./test.sh -m ExtendedSegmentedHashMap -t Microbenchmark -p -e -r " 38 37 25" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -g $nbThread
#  python3 analyse_perf.py perf.log "false" "ExtendedSegmentedHashMap" $nbThread ""

#  perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions,l1d_pend_miss.pending_cycles_any,l2_rqsts.all_demand_miss,cycle_activity.stalls_total -o perf.log ./test.sh -m ConcurrentSkipListMap -t Microbenchmark -p -e -r " 38 37 25" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -g $nbThread
#  python3 analyse_perf.py perf.log "false" "ConcurrentSkipListMap" $nbThread ""
#
#  perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions,l1d_pend_miss.pending_cycles_any,l2_rqsts.all_demand_miss,cycle_activity.stalls_total -o perf.log ./test.sh -m ExtendedSegmentedSkipListMap -t Microbenchmark -p -e -r " 38 37 25" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -g $nbThread
#  python3 analyse_perf.py perf.log "false" "ExtendedSegmentedSkipListMap" $nbThread ""
#
  perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions,l1d_pend_miss.pending_cycles_any,l2_rqsts.all_demand_miss,cycle_activity.stalls_total -o perf.log ./test.sh -m ExtendedSegmentedTreeMap -t Microbenchmark -p -e -r " 12 12 76" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -g $nbThread
  python3 analyse_perf.py perf.log "false" "ExtendedSegmentedTreeMap" $nbThread ""

#  perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions,l1d_pend_miss.pending_cycles_any,l2_rqsts.all_demand_miss,cycle_activity.stalls_total -o perf.log ./test.sh -m SegmentedHashMap -t Microbenchmark -p -e -r "0 0 100" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -g $nbThread
done
#
#python3 compute_avg_throughput_microbenchmark.py "AtomicReference" "1 40 80 120 160"
#python3 compute_avg_throughput_microbenchmark.py "AtomicWriteOnceReference" "1 40 80 120 160"
#
#python3 compute_avg_throughput_microbenchmark.py "CounterJUC" "1 40 80 120 160"
#python3 compute_avg_throughput_microbenchmark.py "CounterIncrementOnly" "1 40 80 120 160"
#python3 compute_avg_throughput_microbenchmark.py "WrappedLongAdder" "1 40 80 120 160"

#python3 compute_avg_throughput_microbenchmark.py "ConcurrentSkipListSet" "1 40 80 120 160"
#python3 compute_avg_throughput_microbenchmark.py "ConcurrentHashSet" "1 40 80 120 160"
#python3 compute_avg_throughput_microbenchmark.py "ExtendedSegmentedHashSet" "1 40 80 120 160"
#
#python3 compute_avg_throughput_microbenchmark.py "ConcurrentLinkedQueue" "2 40 80 120 160"
#python3 compute_avg_throughput_microbenchmark.py "QueueMASP" "2 40 80 120 160"
##
#python3 compute_avg_throughput_microbenchmark.py "ConcurrentHashMap" "40"
#python3 compute_avg_throughput_microbenchmark.py "SegmentedHashMap" "1 40 80 120 160"
#python3 compute_avg_throughput_microbenchmark.py "ExtendedSegmentedHashMap" "40"
#python3 compute_avg_throughput_microbenchmark.py "ConcurrentSkipListMap" "40"
#python3 compute_avg_throughput_microbenchmark.py "ExtendedSegmentedSkipListMap" "40"
python3 compute_avg_throughput_microbenchmark.py "ExtendedSegmentedTreeMap" "1 40 80 120 160"

#python3 analyse_perf.py perf.log "true" "AtomicReference" 0 ""
#python3 analyse_perf.py perf.log "true" "AtomicWriteOnceReference" 0 ""
#
#
#python3 analyse_perf.py perf.log "true" "CounterJUC" 0 ""
#python3 analyse_perf.py perf.log "true" "CounterIncrementOnly" 0 ""
#python3 analyse_perf.py perf.log "true" "LongAdder" 0 ""
#
#python3 analyse_perf.py perf.log "true" "ConcurrentLinkedQueue" 0 ""
#python3 analyse_perf.py perf.log "true" "QueueMASP" 0 ""

#python3 analyse_perf.py perf.log "true" "ConcurrentHashMap" 0 ""
#python3 analyse_perf.py perf.log "true" "ExtendedSegmentedHashMap" 0 ""
#python3 analyse_perf.py perf.log "true" "ConcurrentSkipListMap" 0 ""
#python3 analyse_perf.py perf.log "true" "ExtendedSegmentedSkipListMap" 0 ""
python3 analyse_perf.py perf.log "true" "ExtendedSegmentedTreeMap" 0 ""

#perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions,l1d_pend_miss.pending_cycles_any,l2_rqsts.all_demand_miss,cycle_activity.stalls_total -o perf.log ./test.sh -s SegmentedSkipListSet -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range
#perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions,l1d_pend_miss.pending_cycles_any,l2_rqsts.all_demand_miss,cycle_activity.stalls_total -o perf.log ./test.sh -s ExtendedShardedHashSet -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range
##perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions,l1d_pend_miss.pending_cycles_any,l2_rqsts.all_demand_miss,cycle_activity.stalls_total -o perf.log ./test.sh -s ShardedHashSet -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range
##perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions,l1d_pend_miss.pending_cycles_any,l2_rqsts.all_demand_miss,cycle_activity.stalls_total -o perf.log ./test.sh -s SegmentedTreeSet -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range
##perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions,l1d_pend_miss.pending_cycles_any,l2_rqsts.all_demand_miss,cycle_activity.stalls_total -o perf.log ./test.sh -s ShardedTreeSet -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range
##
##
##perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions,l1d_pend_miss.pending_cycles_any,l2_rqsts.all_demand_miss,cycle_activity.stalls_total -o perf.log ./test.sh -m ShardedHashMap -t Microbenchmark -p -e -r "25 25 50" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -k
#perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions,l1d_pend_miss.pending_cycles_any,l2_rqsts.all_demand_miss,cycle_activity.stalls_total -o perf.log ./test.sh -m SegmentedHashMap -t Microbenchmark -p -e -r "25 25 50" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -k
#perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions,l1d_pend_miss.pending_cycles_any,l2_rqsts.all_demand_miss,cycle_activity.stalls_total -o perf.log ./test.sh -m SegmentedSkipListMap -t Microbenchmark -p -e -r "25 25 50" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -k
##perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions,l1d_pend_miss.pending_cycles_any,l2_rqsts.all_demand_miss,cycle_activity.stalls_total -o perf.log ./test.sh -m SegmentedTreeMap -t Microbenchmark -p -e -r "25 25 50" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -k
#

