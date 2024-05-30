#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

initSize=16384
range=32768
nbTest=1
benchmarkTime=60
warmingUpTime=30
nbThreads=("1" "40" "80")
objects=("ConcurrentSkipListMap" "ExtendedSegmentedSkipListMap")
objects=("ExtendedSegmentedSkipListMap")
ratio="50 50 0"

for object in "${objects[@]}"; do
  python3 rm_file.py "Microbenchmark" "$object"

  for nbThread in "${nbThreads[@]}"; do
    for (( c=1; c<=nbTest; c++ )) do
        perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions,l1d_pend_miss.pending_cycles_any,l2_rqsts.all_demand_miss,cycle_activity.stalls_total -o perf.log ./test.sh -m "$object" -t Microbenchmark -p -e -r "$ratio" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -g "$nbThread"
        python3 analyse_perf.py perf.log "false" "$object" "" "$nbThread"
    done
  done
done
