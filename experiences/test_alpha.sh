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
# ratio="15 15 30 40 0 0"
# ratio="10 10 30 40 5 5"
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
ratio="0 0 30 70 0 0"
# ratio="40 60 0 0 0 0"
ratio="0 100 0 0 0 0"
# ratio="100 0 0 0 0 0"

# alphas=("0.5" "0.7" "0.9" "1.1" "1.3" "1.5" "1.7" "1.9" "2")
# alphas=("0.01")
alphas=("1")

for alpha in "${alphas[@]}";
do
    str_alpha=$(echo "$alpha" | tr '.' '-')
    echo "$str_alpha"
    echo "$alpha"
   for nbUsersInit in 100000 # 1000000
    do
	for nbThread in 2 12 # 12
	do
	    for (( c=1; c<=nbTest; c++ ))
	    do
		echo " =============== > test number : $c"
		# perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions -o perf.log
		./test.sh -c juc.Counter -s ConcurrentHashSet -q Queue -m ConcurrentHashMap -t Retwis -r "$ratio" -p -e -w $benchmarkTime -u $warmingUpTime -h "JUC_$str_alpha" -y $nbUsersInit -d $nbUsersInit -i $nbOps -b -g $nbThread -A $alpha -z
		#  perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions -o perf.log
		# ./test.sh -c CounterIncrementOnly -s SegmentedSkipListSet -q Queue -m ExtendedSegmentedHashMap -t Retwis -r "$ratio" -p -e -w $benchmarkTime -u $warmingUpTime -h "Q_M_C_$str_alpha" -y $nbUsersInit -d $nbUsersInit -i $nbOps -b -g $nbThread -A $alpha -z
	    done
	done
    done
done
