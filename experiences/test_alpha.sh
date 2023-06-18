#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

nbTest=3
benchmarkTime=30
warmingUpTime=30
#nbUsersInit=1000
nbHashCode=10000000
nbOps=100000
ratio="5 15 30 50"

#ExtendedSegmentedConcurrentHash

alphas=("0.5" "0.7" "0.9" "1.1" "1.3" "1.5" "1.7" "1.9" "2")

for alpha in "${alpha[@]}";
do
  # shellcheck disable=SC2001
  str_alpha=$(echo "$alhpa" | sed "s/.//g")

  for nbUsersInit in 100
  do
    # Cleaning old file
    python3 rm_file.py $nbUsersInit "JUC_$str_alpha"
    python3 rm_file.py $nbUsersInit "Q_M_C_$str_alpha"
  #  python3 rm_file.py $nbUsersInit "Q_M_S_C"

    for nbThread in 1 2 4 8 16 32 48
  #  for nbThread in 1 16 48
  #  for nbThread in 1
    do
      for (( c=1; c<=nbTest; c++ ))
      do
        echo " "
        echo " =============== > test number : $c"
        echo " "
        perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions -o perf.log ./test.sh -c Counter -s ConcurrentHashSet -q Queue -m Map -t Retwis -r "$ratio" -p -e -w $benchmarkTime -u $warmingUpTime -h "JUC_$str_alpha" -y $nbUsersInit -d $nbUsersInit -i $nbOps -b -g $nbThread -A $alpha
        python3 analyse_perf.py perf.log "false" "JUC_$str_alpha" $nbThread $nbUsersInit

        perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions -o perf.log ./test.sh -c Counter -s ExtendedSegmentedHashSet -q QueueMASP -m ExtendedSegmentedConcurrentHashMap -t Retwis -r "$ratio" -p -e -w $benchmarkTime -u $warmingUpTime -h "Q_M_C_$str_alpha" -y $nbUsersInit -d $nbUsersInit -i $nbOps -b -g $nbThread -A $alpha
  #      perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions -o perf.log ./test.sh -c Counter -s ExtendedSegmentedHashSet -q QueueMASP -m ExtendedSegmentedConcurrentHashMap -t Retwis -r "$ratio" -p -e -w $benchmarkTime -u $warmingUpTime -h "Q_M_C" -y $nbUsersInit -d $nbUsersInit -i $nbOps -b -g $nbThread
        python3 analyse_perf.py perf.log "false" "Q_M_C_$str_alpha" $nbThread $nbUsersInit

        #perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions -o perf.log ./test.sh -c Counter -s ExtendedConcurrentHashSet -q QueueMASP -m ExtendedSegmentedConcurrentHashMap -t Retwis -r "$ratio" -p -e -w $benchmarkTime -u $warmingUpTime -h "Q_M_S_C" -y $nbUsersInit -d $nbUsersInit -i $nbOps -b -g $nbThread
        #python3 analyse_perf.py perf.log "false" "Q_M_S_C" $nbThread $nbUsersInit
      done
    done
    python3 compute_avg_throughput.py $nbUsersInit "JUC_$str_alpha" "1 2 4 8 16 32 48"
  #  python3 compute_avg_throughput.py $nbUsersInit "JUC" "1 16 48"
  #  python3 compute_avg_throughput.py $nbUsersInit "JUC" "1"
    python3 compute_avg_throughput.py $nbUsersInit "Q_M_C_$str_alpha" "1 2 4 8 16 32 48"
  #  python3 compute_avg_throughput.py $nbUsersInit "Q_M_C" "1 16 48"
  #  python3 compute_avg_throughput.py $nbUsersInit "Q_M_C" "1"
  #  python3 compute_avg_throughput.py $nbUsersInit "Q_M_S_C" "1 2 4 8 16 32 48"
    python3 analyse_perf.py perf.log "true" "JUC_$str_alpha" $nbThread $nbUsersInit
    python3 analyse_perf.py perf.log "true" "Q_M_C_$str_alpha" $nbThread $nbUsersInit
    #python3 analyse_perf.py perf.log "true" "Q_M_S_C" $nbThread $nbUsersInit

  done
done




