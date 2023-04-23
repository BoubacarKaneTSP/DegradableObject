#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

nbTest=10
benchmarkTime=60
warmingUpTime=10
#nbUsersInit=1000
nbHashCode=10000000
nbOps=100000
ratio="0 0 50 50"

for nbUsersInit in 100 #10000 1000000
do
  # Cleaning old file
  for op in "ALL" "ADD" "FOLLOW" "UNFOLLOW" "TWEET" "READ"
  do
    rm "${op}_JUC_139_${nbUsersInit}.txt"
    rm "${op}_Q_M_S_C_139_${nbUsersInit}.txt"
  done

  for nbThread in 1 2 4 8 16 32 48
  do
    for (( c=1; c<=nbTest; c++ ))
    do
	    echo " "
	    echo $nbThread
	    echo " "
      perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions -o perf.log ./test.sh -c Counter -s ConcurrentHashSet -q Queue -m Map -t Retwis -r "$ratio" -p -e -w $benchmarkTime -u $warmingUpTime -h "Q_M_S_C" -y $nbUsersInit -d $nbUsersInit -i $nbOps -b -j -g $nbThread
#      perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions -o perf.log ./test.sh -c CounterIncrementOnly -s ConcurrentHashSet -q Queue -m Map -t Retwis -r "$ratio" -p -e -w $benchmarkTime -u $warmingUpTime -h "Q_M_S_C" -y $nbUsersInit -d $nbUsersInit -i $nbOps -b -j -g $nbThread
      python3 analyse_perf.py perf.log "false" "Q_M_S_C" $nbThread $nbUsersInit
      perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions -o perf.log ./test.sh -c Counter -s ConcurrentHashSet -q Queue -m Map -t Retwis -r "$ratio" -p -e -w $benchmarkTime -u $warmingUpTime -h "JUC" -y $nbUsersInit -d $nbUsersInit -i $nbOps -b -j -g $nbThread
      python3 analyse_perf.py perf.log "false" "JUC" $nbThread $nbUsersInit
    done
  done
  python3 compute_avg_throughput.py $nbUsersInit "JUC" "1 2 4 8 16 32 48"
  python3 compute_avg_throughput.py $nbUsersInit "Q_M_S_C" "1 2 4 8 16 32 48"
  python3 analyse_perf.py perf.log "true" "JUC" $nbThread $nbUsersInit
  python3 analyse_perf.py perf.log "true" "Q_M_S_C" $nbThread $nbUsersInit

done

