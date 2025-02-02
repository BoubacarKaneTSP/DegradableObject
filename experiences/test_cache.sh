#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

nbTest=1
benchmarkTime=20
warmingUpTime=5
nbUsersInits="100000"
nbHashCode=10000000
nbOps=1000000000
ratio="10 10 30 40 5 5"
completion_time="True"
nbThreads=("80")
#nbThreads=("1" "5" "10" "20" "40" "80")
#nbThreads=("1" "40")
declare -a tags=("JUC")
declare -a counters=("CounterJUC")
declare -a sets=("ConcurrentHashSet")
declare -a queues=("ConcurrentLinkedQueue")
declare -a maps=("ConcurrentHashMap")

declare -a arrays=("tags" "counters" "sets" "queues" "maps")
length="${#tags[@]}"
#
#for array in "${arrays[@]}"; do
#    if [ ${#array[@]} -ne $length ]; then
#        echo "number of tags and object should be identical"
#        exit 1
#    fi
#done

for nbUsersInit in "${nbUsersInits[@]}"
do
  for (( i=0; i<length; i++ ));
  do
    python3 rm_file.py "Retwis" "${tags[i]}" $nbUsersInit $completion_time

    for nbThread in "${nbThreads[@]}";
    do
      for (( c=1; c<=nbTest; c++ ))
      do
    	  echo " "
    	  echo " =============== > test number : $c"
    	  echo " "

        perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions,l1d_pend_miss.pending_cycles_any,l2_rqsts.all_demand_miss,cycle_activity.stalls_total -o perf.log ./test.sh -c "${counters[i]}" -s "${sets[i]}" -q "${queues[i]}" -m "${maps[i]}" -t Retwis -r "$ratio" -p -e -w $benchmarkTime -u $warmingUpTime -h "${tags[i]}" -y $nbUsersInit -i $nbOps -b -g $nbThread -z $nbOps
        python3 analyse_perf.py perf.log "false" "${tags[i]}" $nbUsersInit $nbThread "${nbThreads[@]}"

        done
      done

      python3 compute_avg_throughput.py $nbUsersInit "${tags[i]}" $completion_time "${nbThreads[@]}"
      python3 analyse_perf.py perf.log "true" "${tags[i]}" $nbUsersInit " " "${nbThreads[@]}"

  done

done


