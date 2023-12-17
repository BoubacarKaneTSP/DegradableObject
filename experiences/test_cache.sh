#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

nbTest=5
benchmarkTime=60
warmingUpTime=30
#nbUsersInit=1000
nbHashCode=10000000
nbOps=50000000000
ratio="0 0 0 0 100 0"
#ratio="5 10 20 35 15 15"
completion_time="False"
#ExtendedSegmentedConcurrentHash

for nbUsersInit in 1000
do
  #  Cleaning old file
#  python3 rm_file.py $nbUsersInit "JUC" $completion_time
  python3 rm_file.py $nbUsersInit "Q_M_C" $completion_time
#  python3 rm_file.py $nbUsersInit "SEQ" $completion_time
#  rm JUC_${nbUsersInit}_gc_usage.txt
#  rm Q_M_C_${nbUsersInit}_gc_usage_ExtHashMapNoCleanTL.txt
#  rm SEQ_${nbUsersInit}_gc_usage.txt

#  python3 rm_file.py $nbUsersInit "Q_M_S_C"

#  for nbThread in 1 2 4 8 16 32 48 64 70 86 96
#  for nbThread in 1 2 4
#  for nbThread in 2 16 48
  for nbThread in 1 32 96
#  for nbThread in 96
#  for nbThread in 1 4 8
#  for nbThread in 1
  do
    for (( c=1; c<=nbTest; c++ ))
    do
	    echo " "
	    echo " =============== > test number : $c"
	    echo " "

#      perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions,l1d_pend_miss.pending_cycles_any,l2_rqsts.all_demand_miss,cycle_activity.stalls_total -o perf.log ./test.sh -c CounterIncrementOnly -s HashSet -q SequentialQueue -m ExtendedSegmentedHashMap -t Retwis -r "$ratio" -p -e -w $benchmarkTime -u $warmingUpTime -h "SEQ" -y $nbUsersInit -d $nbUsersInit -i $nbOps -b -g $nbThread -j #-z $nbOps
#      python3 analyse_perf.py perf.log "false" "SEQ" $nbThread $nbUsersInit

      perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions,l1d_pend_miss.pending_cycles_any,l2_rqsts.all_demand_miss,cycle_activity.stalls_total -o perf.log ./test.sh -c CounterIncrementOnly -s ConcurrentHashSet -q QueueMASP -m Map -t Retwis -r "$ratio" -p -e -w $benchmarkTime -u $warmingUpTime -h "Q_M_C" -y $nbUsersInit -d $nbUsersInit -i $nbOps -b -g $nbThread -j #-z $nbOps
      python3 analyse_perf.py perf.log "false" "Q_M_C" $nbThread $nbUsersInit
#      perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions -o perf.log ./test.sh -c Counter -s ExtendedSegmentedHashSet -q QueueMASP -m ExtendedSegmentedConcurrentHashMap -t Retwis -r "$ratio" -p -e -w $benchmarkTime -u $warmingUpTime -h "Q_M_C" -y $nbUsersInit -d $nbUsersInit -i $nbOps -b -g $nbThread
#
#      perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions,l1d_pend_miss.pending_cycles_any,l2_rqsts.all_demand_miss,cycle_activity.stalls_total -o perf.log ./test.sh -c CounterJUC -s ConcurrentHashSet -q Queue -m Map -t Retwis -r "$ratio" -p -e -w $benchmarkTime -u $warmingUpTime -h "JUC" -y $nbUsersInit -d $nbUsersInit -i $nbOps -b -g $nbThread -j #-z $nbOps
#      python3 analyse_perf.py perf.log "false" "JUC" $nbThread $nbUsersInit

      #perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions -o perf.log ./test.sh -c Counter -s ExtendedConcurrentHashSet -q QueueMASP -m ExtendedSegmentedConcurrentHashMap -t Retwis -r "$ratio" -p -e -w $benchmarkTime -u $warmingUpTime -h "Q_M_S_C" -y $nbUsersInit -d $nbUsersInit -i $nbOps -b -g $nbThread
      #python3 analyse_perf.py perf.log "false" "Q_M_S_C" $nbThread $nbUsersInit
    done
  done
#  python3 compute_avg_throughput.py $nbUsersInit "JUC" "1 2 4 8 16 32 48 64 70 86 96" $completion_time
#  python3 compute_avg_throughput.py $nbUsersInit "JUC" "1 2 4 8 16 32 48" $completion_time
#  python3 compute_avg_throughput.py $nbUsersInit "JUC" "2 16 48" $completion_time
#  python3 compute_avg_throughput.py $nbUsersInit "JUC" "1 32 96" $completion_time
#  python3 compute_avg_throughput.py $nbUsersInit "JUC" "48" $completion_time
#  python3 compute_avg_throughput.py $nbUsersInit "JUC" "1 2 4" $completion_time
#  python3 compute_avg_throughput.py $nbUsersInit "JUC" "1" $completion_time

#  python3 compute_avg_throughput.py $nbUsersInit "Q_M_C" "1 2 4 8 16 32 48 64 70 86 96" $completion_time
#  python3 compute_avg_throughput.py $nbUsersInit "Q_M_C" "1 2 4 8 16 32 48" $completion_time
# python3 compute_avg_throughput.py $nbUsersInit "Q_M_C" "2 16 48" $completion_time
 python3 compute_avg_throughput.py $nbUsersInit "Q_M_C" "1 32 96" $completion_time
# python3 compute_avg_throughput.py $nbUsersInit "Q_M_C" "48" $completion_time
# python3 compute_avg_throughput.py $nbUsersInit "Q_M_C" "1 2 4" $completion_time
#  python3 compute_avg_throughput.py $nbUsersInit "Q_M_C" "1" $completion_time

#  python3 compute_avg_throughput.py $nbUsersInit "SEQ" "1 2 4 8 16 32 48 64 70 86 96" $completion_time
#  python3 compute_avg_throughput.py $nbUsersInit "SEQ" "1 2 4 8 16 32 48" $completion_time
# python3 compute_avg_throughput.py $nbUsersInit "SEQ" "1 2 4" $completion_time
# python3 compute_avg_throughput.py $nbUsersInit "SEQ" "1 32 96" $completion_time
# python3 compute_avg_throughput.py $nbUsersInit "SEQ" "96" $completion_time
#  python3 compute_avg_throughput.py $nbUsersInit "SEQ" "1" $completion_time

#  python3 compute_avg_gc.py $nbUsersInit "JUC" "1 2 4 8 16 32 48 64 70 86 96" $completion_time
#  python3 compute_avg_gc.py $nbUsersInit "Q_M_C" "1 2 4 8 16 32 48 64 70 86 96" $completion_time
#  python3 compute_avg_gc.py $nbUsersInit "SEQ" "1 2 4 8 16 32 48 64 70 86 96" $completion_time

#  python3 compute_avg_gc.py $nbUsersInit "JUC" "1 32 96" $completion_time
  python3 compute_avg_gc.py $nbUsersInit "Q_M_C" "1 32 96" $completion_time
#  python3 compute_avg_gc.py $nbUsersInit "SEQ" "1 32 96" $completion_time
#
#  python3 compute_avg_gc.py $nbUsersInit "JUC" "1 32 96" $completion_time
  python3 compute_avg_gc.py $nbUsersInit "Q_M_C" "1 32 96" $completion_time
#  python3 compute_avg_gc.py $nbUsersInit "SEQ" "1 32 96" $completion_time

#  python3 analyse_perf.py perf.log "true" "JUC" $nbThread $nbUsersInit
  python3 analyse_perf.py perf.log "true" "Q_M_C" $nbThread $nbUsersInit
#  python3 analyse_perf.py perf.log "true" "SEQ" $nbThread $nbUsersInit

done


