#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

nbTest=1
benchmarkTime=20
warmingUpTime=20
#nbUsersInit=1000
nbHashCode=10000000
nbOps=10000000
# ADD, FOLLOW/UNFOLLOW, TWEET, READ, GROUP, PROFILE
ratio="15 15 30 40 0 0"
ratio="10 10 30 40 5 5"
# ratio="0 0 0 0 0 100"
# ratio="0 0 99 1 0 0"
# ratio="10 20 0 0 30 40"
# ratio="15 10 20 30 5 20"
# ratio="0 0 0 50 50 0"
# ratio="0 0 0 0 0 100"
# ratio="0 0 20 80 0 0"
# ratio="0 100 0 0 0 0"
# ratio="0 0 0 0 0 100"
# ratio="0 0 0 0 100 0"
# ratio="0 0 30 70 0 0"
# ratio="40 60 0 0 0 0"
# ratio="0 100 0 0 0 0"
# ratio="100 0 0 0 0 0"

#ExtendedSegmentedConcurrentHash

# alphas=("0.5" "0.7" "0.9" "1.1" "1.3" "1.5" "1.7" "1.9" "2")
# alphas=("1000000")
alphas=("1")

for alpha in "${alphas[@]}";
do
  str_alpha=$(echo "$alpha" | tr '.' '-')
  echo "$str_alpha"
  echo "$alpha"
  for nbUsersInit in 12
  do
    # Cleaning old file
    # python3 rm_file.py $nbUsersInit "JUC_$str_alpha"
      # python3 rm_file.py $nbUsersInit "Q_M_C_$str_alpha"
  #  python3 rm_file.py $nbUsersInit "Q_M_S_C"

    # for nbThread in 1 2 4 8 16 32 48
      #  for nbThread in 1 16 48
      for nbThread in 1
      # for nbThread in 1 2 4 6 8 10 12
    do
      for (( c=1; c<=nbTest; c++ ))
      do
          echo " =============== > test number : $c"
          # perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions -o perf.log
	  # ./test.sh -c juc.Counter -s HashSet -q Queue -m HashMap -t Retwis -r "$ratio" -p -e -w $benchmarkTime -u $warmingUpTime -h "JUC_$str_alpha" -y $nbUsersInit -d $nbUsersInit -i $nbOps -b -g $nbThread -A $alpha -z # SEQ
	  # ./test.sh -c juc.Counter -s ConcurrentHashSet -q Queue -m juc.LockBasedJavaHashMap -t Retwis -r "$ratio" -p -e -w $benchmarkTime -u $warmingUpTime -h "JUC_$str_alpha" -y $nbUsersInit -d $nbUsersInit -i $nbOps -b -g $nbThread -A $alpha -z
	  ./test.sh -c juc.Counter -s ConcurrentHashSet -q Queue -m juc.ConcurrentHashMap -t Retwis -r "$ratio" -p -e -w $benchmarkTime -u $warmingUpTime -h "JUC_$str_alpha" -y $nbUsersInit -d $nbUsersInit -i $nbOps -b -g $nbThread -A $alpha -z
	  # perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions -o perf.log
	  # ./test.sh -c CounterIncrementOnly -s SegmentedHashSet -q QueueMASP -m ExtendedSegmentedHashMap -t Retwis -r "$ratio" -p -e -w $benchmarkTime -u $warmingUpTime -h "Q_M_C_$str_alpha" -y $nbUsersInit -d $nbUsersInit -i $nbOps -b -g $nbThread -A $alpha -z
      done
    done
  #   python3 compute_avg_throughput.py $nbUsersInit "JUC_$str_alpha" "1 2 4 8 16 32 48"
  # #  python3 compute_avg_throughput.py $nbUsersInit "JUC" "1 16 48"
  # #  python3 compute_avg_throughput.py $nbUsersInit "JUC" "1"
  #   python3 compute_avg_throughput.py $nbUsersInit "Q_M_C_$str_alpha" "1 2 4 8 16 32 48"
  # #  python3 compute_avg_throughput.py $nbUsersInit "Q_M_C" "1 16 48"
  # #  python3 compute_avg_throughput.py $nbUsersInit "Q_M_C" "1"
  # #  python3 compute_avg_throughput.py $nbUsersInit "Q_M_S_C" "1 2 4 8 16 32 48"
  #   python3 analyse_perf.py perf.log "true" "JUC_$str_alpha" $nbThread $nbUsersInit
  #   python3 analyse_perf.py perf.log "true" "Q_M_C_$str_alpha" $nbThread $nbUsersInit
  #   #python3 analyse_perf.py perf.log "true" "Q_M_S_C" $nbThread $nbUsersInit

  done
done
