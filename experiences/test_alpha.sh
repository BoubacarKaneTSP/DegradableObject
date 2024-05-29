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
# ratio="0 0 0 0 0 100"
# ratio="10 20 0 0 30 40"
# ratio="15 10 20 30 5 20"
# ratio="0 0 0 50 50 0"
# ratio="0 0 0 0 0 100"
# ratio="0 0 20 80 0 0"
# ratio="0 100 0 0 0 0"
# ratio="0 0 0 0 0 100"
# ratio="0 0 0 0 50 50"
# ratio="0 0 1 99 0 0"
# ratio="40 60 0 0 0 0"
# ratio="0 100 0 0 0 0"
# ratio="100 0 0 0 0 0"
 ratio="5 5 15 60 5 10"
#ratio="5 15 30 50 0 0"

# alphas=("0.5" "0.7" "0.9" "1.1" "1.3" "1.5" "1.7" "1.9" "2")
# alphas=("0.01")
 alphas=("1")
# alphas=("0.01" "0.1" "1")
#alphas=("0.1" "1")

declare -A params
#params[seq]="-c juc.Counter -s HashSet -q LinkedList -m SkipListMap"
params[juc]="-c juc.Counter -s ConcurrentHashSet -q Queue -m ConcurrentSkipListMap"
params[dego]="-c CounterIncrementOnly -s SegmentedHashSet -q Queue -m ConcurrentSkipListMap"
#params[dego]="-c CounterIncrementOnly -s SegmentedHashSet -q QueueMASP -m ExtendedSegmentedSkipListMap"
params[dap]="-c CounterIncrementOnly -s ShardedHashSet -q ShardedLinkedList -m ShardedSkipListMap"

for impl in dego;
do
    for alpha in "${alphas[@]}";
    do
	for nbUsersInit in 100000 #500000 1000000
	do
#	    for nbThread in 1 5 10 20 40 80
	    for nbThread in 1 40 80
	    do
		nbOps=$((1000000*nbThread))
		for (( c=1; c<=nbTest; c++ ))
		do
		    # perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions -o perf.log
 		    perf=$(./test.sh ${params["${impl}"]} -t Retwis -r "$ratio" -p -e -w $benchmarkTime -u $warmingUpTime -h "JUC_$str_alpha" -y $nbUsersInit -d $nbUsersInit -i $nbOps -b -g $nbThread -A $alpha -z | grep -i "completion time :" | awk '{print $6}' | sed s/\(//g)
		    echo ${impl}";"${alpha}";"${nbUsersInit}";"${nbThread}";"${perf}
# 		    ./test.sh ${params["${impl}"]} -t Retwis -r "$ratio" -p -e -w $benchmarkTime -u $warmingUpTime -h "JUC_$str_alpha" -y $nbUsersInit -d $nbUsersInit -i $nbOps -b -g $nbThread -A $alpha -z
		    #  perf stat --no-big-num -d -e cache-references,cache-misses,branches,branch-misses,cycles,instructions -o perf.log
		    # ./test.sh -c CounterIncrementOnly -s ConcurrentHashSet -q QueueMASP -m ConcurrentSkipListMap -t Retwis -r "$ratio" -p -e -w $benchmarkTime -u $warmingUpTime -h "Q_M_C_$str_alpha" -y $nbUsersInit -d $nbUsersInit -i $nbOps -b -g $nbThread -A $alpha -z
		    # ./test.sh -c CounterIncrementOnly -s SegmentedHashSet -q Queue -m ConcurrentSkipListMap -t Retwis -r "$ratio" -p -e -w $benchmarkTime -u $warmingUpTime -h "Q_M_C_$str_alpha" -y $nbUsersInit -d $nbUsersInit -i $nbOps -b -g $nbThread -A $alpha -z
		    # ./test.sh -c CounterIncrementOnly -s ConcurrentHashSet -q Queue -m ExtendedSegmentedSkipListMap -t Retwis -r "$ratio" -p -e -w $benchmarkTime -u $warmingUpTime -h "Q_M_C_$str_alpha" -y $nbUsersInit -d $nbUsersInit -i $nbOps -b -g $nbThread -A $alpha -z
		done
	    done
	done
    done
done
