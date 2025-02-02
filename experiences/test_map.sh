#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

initSize=1024
range=2048
nbTest=5
benchmarkTime=30
warmingUpTime=20

#for type in "ConcurrentSkipListSet" "SegmentedSkipListSet" "SegmentedTreeSet" "ConcurrentHashSet" "SegmentedHashSet" "ConcurrentHashMap" "SegmentedHashMap" "ConcurrentLinkedQueue" "QueueMASP" "CounterJUC" "CounterIncrementOnly" "WrappedLongAdder"
#for type in "ConcurrentHashMap" "ExtendedSegmentedConcurrentHashMap" "ConcurrentSkipListMap" "ExtendedSegmentedSkipListMap"
for type in "ConcurrentSkipListMap" "ExtendedSegmentedSkipListMap"
do

  python3 rm_file_microbenchmark.py $type

  for nbThread in 1 2 4 8 16 32 48
  #for nbThread in 1 16 48
  #for nbThread in 1
  do
    for (( c=1; c<=nbTest; c++ ))
    do
      echo " "
      echo " =============== > test number [$c] for [$nbThread] thread(s) with [$type]"
      echo " "

      if [[ $type == "ConcurrentLinkedQueue" || $type == "QueueMASP" ]]
      then
        perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions,l1d_pend_miss.pending_cycles_any,l2_rqsts.all_demand_miss,cycle_activity.stalls_total -o perf.log ./test.sh -q $type -t Microbenchmark -p -e -r "100 0 0" -w $benchmarkTime -u $warmingUpTime -i $initSize -a -d $range -g $nbThread
      elif [[ $type == "CounterJUC" || $type == "CounterIncrementOnly" || $type == "WrappedLongAdder" ]]
      then
        perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions,l1d_pend_miss.pending_cycles_any,l2_rqsts.all_demand_miss,cycle_activity.stalls_total -o perf.log ./test.sh -q $type -t Microbenchmark -p -e -r "100 0 0" -w $benchmarkTime -u $warmingUpTime -i $initSize -d $range -g $nbThread
      else
        perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions,l1d_pend_miss.pending_cycles_any,l2_rqsts.all_demand_miss,cycle_activity.stalls_total -o perf.log ./test.sh -q $type -t Microbenchmark -p -e -r "25 25 50" -w $benchmarkTime -u $warmingUpTime -i $initSize -d $range -g $nbThread
      fi

      python3 analyse_perf_microbenchmark.py perf.log "false" $type $nbThread

    done
  done

  python3 compute_avg_throughput_microbenchmark.py $type "1 2 4 8 16 32 48"
  python3 analyse_perf_microbenchmark.py perf.log "true" $type $nbThread

done



#python3 compute_avg_throughput_microbenchmark.py "ConcurrentLinkedQueue" "1 2 4 8 16 32 48"
#python3 analyse_perf_microbenchmark.py perf.log "true" "ConcurrentLinkedQueue" $nbThread
#
#python3 compute_avg_throughput_microbenchmark.py "QueueMASP" "1 2 4 8 16 32 48"
#python3 analyse_perf_microbenchmark.py perf.log "true" "QueueMASP" $nbThread

#perf stat -B -e cache-references,cache-misses ./test.sh -s ConcurrentHashSet -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -j
#perf stat -B -e cache-references,cache-misses ./test.sh -s SegmentedHashSet -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -j
#perf stat -B -e cache-references,cache-misses ./test.sh -s ShardedHashSet -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -j
#perf stat -B -e cache-references,cache-misses ./test.sh -s SegmentedTreeSet -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -j
#perf stat -B -e cache-references,cache-misses ./test.sh -s ShardedTreeSet -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -j

#perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions,l1d_pend_miss.pending_cycles_any,l2_rqsts.all_demand_miss -o perf.log ./test.sh -s Queue -t Microbenchmark -p -e -r "100 0 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -a -d $range -j
#python3 analyse_perf.py perf.log "false" "Queue" $nbThread $nbUsersInit

#perf stat -B -e cache-references,cache-misses ./test.sh -s QueueMASP -t Microbenchmark -p -e -r "100 0 0" -w $benchmarkTime -u $warmingUpTime -n 15 -i $initSize -a -d $range -j

#perf stat -B -e cache-references,cache-misses ./test.sh -s Map -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -j -k
#perf stat -B -e cache-references,cache-misses ./test.sh -s ShardedHashMap -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -j -k
#perf stat -B -e cache-references,cache-misses ./test.sh -s SegmentedHashMap -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -j -k
#perf stat -B -e cache-references,cache-misses ./test.sh -s ConcurrentSkipListMap -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -j -k
#perf stat -B -e cache-references,cache-misses ./test.sh -s SegmentedSkipListMap -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -j -k
#perf stat -B -e cache-references,cache-misses ./test.sh -s SegmentedTreeMap -t Microbenchmark -p -e -r "50 50 0" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -j -k

