#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

./test.sh -x

nbTest=10
benchmarkTime=20
warmingUpTime=5
nbHashCode=10000000
nbOps=10000000
nbThreads=("1" "5" "10" "20" "40" "80")

# ADD, FOLLOW/UNFOLLOW, TWEET, READ, GROUP, PROFILE
ratio="5 5 15 60 5 10"

#alphas=("0.01" "0.1" "1")
#alphas=("0.1")

declare -A params

# Experience Figure 9
echo "Figure 9"
params[juc]="-c juc.Counter -s ConcurrentHashSet -q Queue -m ConcurrentSkipListMap"
params[dego]="-c CounterIncrementOnly -s ConcurrentHashSet -q QueueMASP -m ExtendedSegmentedSkipListMap"

alphas=("1")

> results_retwis_fig_9.txt

for impl in "${!params[@]}";
do
    for alpha in "${alphas[@]}";
    do
        for nbUsersInit in 100000 500000 1000000
        do
            for nbThread in "${nbThreads[@]}";
            do
                echo ${nbThread}
                nbOps=$((1000000*nbThread))
                for (( c=1; c<=nbTest; c++ ))
                do
#                    ./test.sh ${params["${impl}"]} -t Retwis -r "$ratio" -p -e -w $benchmarkTime -u $warmingUpTime -h "${impl}_${nbThread}" -y $nbUsersInit -d $nbUsersInit -i $nbOps -b -g $nbThread -A $alpha -z
                    perf=$(./test.sh ${params["${impl}"]} -t Retwis -r "$ratio" -p -e -w $benchmarkTime -u $warmingUpTime -h "${impl}_${nbThread}" -y $nbUsersInit -d $nbUsersInit -i $nbOps -b -g $nbThread -A $alpha -z | grep -i "completion time :" | awk '{print $6}' | sed s/\(//g)
                    echo ${impl}";"${alpha}";"${nbUsersInit}";"${nbThread}";"${perf} >> results_retwis_fig_9.txt
                done
            done
        done
    done
done

params=()
params[dap]="-c CounterIncrementOnly -s ShardedHashSet -q ShardedLinkedList -m ShardedSkipListMap"

for impl in "${!params[@]}";
do
    for alpha in "${alphas[@]}";
    do
        for nbUsersInit in 100000 500000 1000000
        do
            for nbThread in "${nbThreads[@]}";
            do
                nbOps=$((1000000*nbThread))
                for (( c=1; c<=nbTest; c++ ))
                do
                    perf=$(./test.sh ${params["${impl}"]} -t Retwis -r "$ratio" -p -e -w $benchmarkTime -u $warmingUpTime -h "${impl}_${nbThread}" -y $nbUsersInit -d $nbUsersInit -i $nbOps -b -g $nbThread -A $alpha -z -D | grep -i "completion time :" | awk '{print $6}' | sed s/\(//g)
                    echo ${impl}";"${alpha}";"${nbUsersInit}";"${nbThread}";"${perf} >> results_retwis_fig_9.txt
                done
            done
        done
    done
done

python3.11 generate_retwis_graph.py -f results_retwis_fig_9.txt -o results/retwis.tex -t 1

# Experience Figure 10
echo "Figure 10"
params=()
params[juc]="-c juc.Counter -s ConcurrentHashSet -q Queue -m ConcurrentSkipListMap"

alphas=("0.01" "0.1" "1")

> results_retwis_fig_10.txt

for impl in "${!params[@]}";
do
    for alpha in "${alphas[@]}";
    do
        for nbUsersInit in 1000000
        do
            for nbThread in "${nbThreads[@]}";
            do
                nbOps=$((1000000*nbThread))
                for (( c=1; c<=nbTest; c++ ))
                do
                    perf=$(./test.sh ${params["${impl}"]} -t Retwis -r "$ratio" -p -e -w $benchmarkTime -u $warmingUpTime -h "${impl}_${nbThread}" -y $nbUsersInit -d $nbUsersInit -i $nbOps -b -g $nbThread -A $alpha -z -D | grep -i "completion time :" | awk '{print $6}' | sed s/\(//g)
                    echo ${impl}";"${alpha}";"${nbUsersInit}";"${nbThread}";"${perf} >> results_retwis_fig_10.txt
                done
            done
        done
    done
done

params=()
params[dap]="-c CounterIncrementOnly -s ShardedHashSet -q ShardedLinkedList -m ShardedSkipListMap"

for impl in "${!params[@]}";
do
    for alpha in "${alphas[@]}";
    do
        for nbUsersInit in 1000000
        do
            for nbThread in "${nbThreads[@]}";
            do
                nbOps=$((1000000*nbThread))
                for (( c=1; c<=nbTest; c++ ))
                do
                    perf=$(./test.sh ${params["${impl}"]} -t Retwis -r "$ratio" -p -e -w $benchmarkTime -u $warmingUpTime -h "${impl}_${nbThread}" -y $nbUsersInit -d $nbUsersInit -i $nbOps -b -g $nbThread -A $alpha -z -D | grep -i "completion time :" | awk '{print $6}' | sed s/\(//g)
                    echo ${impl}";"${alpha}";"${nbUsersInit}";"${nbThread}";"${perf} >> results_retwis_fig_10.txt
                done
            done
        done
    done
done

params=()
params[dego]="-c CounterIncrementOnly -s ConcurrentHashSet -q QueueMASP -m ExtendedSegmentedSkipListMap"

for impl in "${!params[@]}";
do
    for alpha in "${alphas[@]}";
    do
        for nbUsersInit in 1000000
        do
            for nbThread in "${nbThreads[@]}";
            do
                nbOps=$((1000000*nbThread))
                for (( c=1; c<=nbTest; c++ ))
                do
                    perf=$(./test.sh ${params["${impl}"]} -t Retwis -r "$ratio" -p -e -w $benchmarkTime -u $warmingUpTime -h "${impl}_${nbThread}" -y $nbUsersInit -d $nbUsersInit -i $nbOps -b -g $nbThread -A $alpha -z -D | grep -i "completion time :" | awk '{print $6}' | sed s/\(//g)
                    echo ${impl}";"${alpha}";"${nbUsersInit}";"${nbThread}";"${perf} >> results_retwis_fig_10.txt
                done
            done
        done
    done
done

python3.11 generate_retwis_graph.py -f results_retwis_fig_10.txt -o results/retwis_alpha.tex -t 2

# Experience Figure 6
echo "Figure 6"
initSize=16384
range=32768
benchmarkTime=60
warmingUpTime=30
nbTest=30
objects=("Counter" "CounterIncrementOnly" "LongAdder" "ConcurrentHashMap" "ExtendedSegmentedHashMap" "ConcurrentSkipListMap" "ExtendedSegmentedSkipListMap")
ratio="100 0 0"

for object in "${objects[@]}"; do
  python3.11 rm_file.py "Microbenchmark" "$object"
  for nbThread in "${nbThreads[@]}"; do
    for (( c=1; c<=nbTest; c++ )) do
        ./test.sh -m "$object" -t Microbenchmark -p -e -r "$ratio" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -g "$nbThread"
    done
  done
  python3.11 compute_avg_throughput.py -t "$object" -typeOp ALL -p microbenchmark_results/avg_perf -u 1000
done

python3.11 generate_latex.py -f microbenchmark_results/avg_perf/ConcurrentHashMap_ALL.txt -f microbenchmark_results/avg_perf/ExtendedSegmentedHashMap_ALL.txt -t results/HashMap
python3.11 generate_latex.py -f microbenchmark_results/avg_perf/ConcurrentSkipListMap_ALL.txt -f microbenchmark_results/avg_perf/ExtendedSegmentedSkipListMap_ALL.txt -t results/SkipListMap
python3.11 generate_latex.py -f microbenchmark_results/avg_perf/Counter_ALL.txt -f microbenchmark_results/avg_perf/CounterIncrementOnly_ALL.txt -f microbenchmark_results/avg_perf/LongAdder_ALL.txt -t results/Counter

# Saving 100% update results for Figure 7
python3.11 compute_avg_throughput.py -t ConcurrentHashMap -typeOp ALL -p microbenchmark_results/avg_perf/100 -u 1000 -d
python3.11 compute_avg_throughput.py -t ConcurrentSkipListMap -typeOp ALL -p microbenchmark_results/avg_perf/100 -u 1000 -d
python3.11 compute_avg_throughput.py -t ExtendedSegmentedHashMap -typeOp ALL -p microbenchmark_results/avg_perf/100 -u 1000 -d
python3.11 compute_avg_throughput.py -t ExtendedSegmentedSkipListMap -typeOp ALL -p microbenchmark_results/avg_perf/100 -u 1000 -d

# Saving 16k dataset size for Figure 8
python3.11 compute_avg_throughput.py -t ConcurrentHashMap -typeOp ALL -p microbenchmark_results/avg_perf/16k -u 1000 -d
python3.11 compute_avg_throughput.py -t ExtendedSegmentedHashMap -typeOp ALL -p microbenchmark_results/avg_perf/16k -u 1000 -d

objects=("ConcurrentLinkedQueue" "QueueMASP")

for object in "${objects[@]}"; do
  python3.11 rm_file.py "Microbenchmark" "$object"

  for nbThread in "${nbThreads[@]}"; do
    for (( c=1; c<=nbTest; c++ )) do
        ./test.sh -m "$object" -t Microbenchmark -e -r "$ratio" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -g "$nbThread" -a
    done
  done
  python3.11 compute_avg_throughput.py -t "$object" -typeOp ALL -p microbenchmark_results/avg_perf -u 1000
done

python3.11 generate_latex.py -f microbenchmark_results/avg_perf/ConcurrentLinkedQueue_ALL.txt -f microbenchmark_results/avg_perf/QueueMASP_ALL.txt -t results/Queue

objects=("AtomicWriteOnceReference" "AtomicReference")
ratio="0 100 0"

for object in "${objects[@]}"; do
  python3.11 rm_file.py "Microbenchmark" "$object"

  for nbThread in "${nbThreads[@]}"; do
    for (( c=1; c<=nbTest; c++ )) do
        ./test.sh -m "$object" -t Microbenchmark -e -r "$ratio" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -g "$nbThread"
    done
  done

  python3.11 compute_avg_throughput.py -t "$object" -typeOp ALL -p microbenchmark_results/avg_perf -u 1000
done

python3.11 generate_latex.py -f microbenchmark_results/avg_perf/AtomicReference_ALL.txt -f microbenchmark_results/avg_perf/AtomicWriteOnceReference_ALL.txt -t results/Reference

######################### Experience Figure 7
echo "Figure 7"

objects=("ConcurrentHashMap" "ExtendedSegmentedHashMap" "ConcurrentSkipListMap" "ExtendedSegmentedSkipListMap")
ratio="25 75 0"

for object in "${objects[@]}"; do
  python3.11 rm_file.py "Microbenchmark" "$object"

  for nbThread in "${nbThreads[@]}"; do
    for (( c=1; c<=nbTest; c++ )) do
        ./test.sh -m "$object" -t Microbenchmark -p -e -r "$ratio" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -g "$nbThread"
    done
  done
done

python3.11 compute_avg_throughput.py -t ConcurrentHashMap -typeOp ALL -p microbenchmark_results/avg_perf/25 -u 1000 -d
python3.11 compute_avg_throughput.py -t ConcurrentSkipListMap -typeOp ALL -p microbenchmark_results/avg_perf/25 -u 1000 -d
python3.11 compute_avg_throughput.py -t ExtendedSegmentedHashMap -typeOp ALL -p microbenchmark_results/avg_perf/25 -u 1000 -d
python3.11 compute_avg_throughput.py -t ExtendedSegmentedSkipListMap -typeOp ALL -p microbenchmark_results/avg_perf/25 -u 1000 -d

ratio="50 50 0"

for object in "${objects[@]}"; do
  python3.11 rm_file.py "Microbenchmark" "$object"

  for nbThread in "${nbThreads[@]}"; do
    for (( c=1; c<=nbTest; c++ )) do
        ./test.sh -m "$object" -t Microbenchmark -p -e -r "$ratio" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -g "$nbThread"
    done
  done
done

python3.11 compute_avg_throughput.py -t ConcurrentHashMap -typeOp ALL -p microbenchmark_results/avg_perf/50 -u 1000 -d
python3.11 compute_avg_throughput.py -t ConcurrentSkipListMap -typeOp ALL -p microbenchmark_results/avg_perf/50 -u 1000 -d
python3.11 compute_avg_throughput.py -t ExtendedSegmentedHashMap -typeOp ALL -p microbenchmark_results/avg_perf/50 -u 1000 -d
python3.11 compute_avg_throughput.py -t ExtendedSegmentedSkipListMap -typeOp ALL -p microbenchmark_results/avg_perf/50 -u 1000 -d

ratio="75 25 0"

for object in "${objects[@]}"; do
  python3.11 rm_file.py "Microbenchmark" "$object"

  for nbThread in "${nbThreads[@]}"; do
    for (( c=1; c<=nbTest; c++ )) do
        ./test.sh -m "$object" -t Microbenchmark -p -e -r "$ratio" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -g "$nbThread"

    done
  done
done

python3.11 compute_avg_throughput.py -t ConcurrentHashMap -typeOp ALL -p microbenchmark_results/avg_perf/75 -u 1000 -d
python3.11 compute_avg_throughput.py -t ConcurrentSkipListMap -typeOp ALL -p microbenchmark_results/avg_perf/75 -u 1000 -d
python3.11 compute_avg_throughput.py -t ExtendedSegmentedHashMap -typeOp ALL -p microbenchmark_results/avg_perf/75 -u 1000 -d
python3.11 compute_avg_throughput.py -t ExtendedSegmentedSkipListMap -typeOp ALL -p microbenchmark_results/avg_perf/75 -u 1000 -d

python3.11 generate_histograme_latex.py -f ExtendedSegmentedHashMap_ALL.txt -f ExtendedSegmentedSkipListMap_ALL.txt -f ConcurrentHashMap_ALL.txt -f ConcurrentSkipListMap_ALL.txt -u 25
python3.11 generate_histograme_latex.py -f ExtendedSegmentedHashMap_ALL.txt -f ExtendedSegmentedSkipListMap_ALL.txt -f ConcurrentHashMap_ALL.txt -f ConcurrentSkipListMap_ALL.txt -u 50
python3.11 generate_histograme_latex.py -f ExtendedSegmentedHashMap_ALL.txt -f ExtendedSegmentedSkipListMap_ALL.txt -f ConcurrentHashMap_ALL.txt -f ConcurrentSkipListMap_ALL.txt -u 75
python3.11 generate_histograme_latex.py -f ExtendedSegmentedHashMap_ALL.txt -f ExtendedSegmentedSkipListMap_ALL.txt -f ConcurrentHashMap_ALL.txt -f ConcurrentSkipListMap_ALL.txt -u 100

################## Figure 8
echo "Figure 8"

objects=("ConcurrentHashMap" "ExtendedSegmentedHashMap")
ratio="100 0 0"

initSize=32768
range=65536

for object in "${objects[@]}"; do
  python3.11 rm_file.py "Microbenchmark" "$object"

  for nbThread in "${nbThreads[@]}"; do
    for (( c=1; c<=nbTest; c++ )) do
        ./test.sh -m "$object" -t Microbenchmark -p -e -r "$ratio" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -g "$nbThread"

    done
  done
done

python3.11 compute_avg_throughput.py -t ConcurrentHashMap -typeOp ALL -p microbenchmark_results/avg_perf/32k -u 1000 -d
python3.11 compute_avg_throughput.py -t ExtendedSegmentedHashMap -typeOp ALL -p microbenchmark_results/avg_perf/32k -u 1000 -d

initSize=65536
range=131072

for object in "${objects[@]}"; do
  python3.11 rm_file.py "Microbenchmark" "$object"

  for nbThread in "${nbThreads[@]}"; do
    for (( c=1; c<=nbTest; c++ )) do
        ./test.sh -m "$object" -t Microbenchmark -p -e -r "$ratio" -w $benchmarkTime -u $warmingUpTime -n $nbTest -i $initSize -d $range -g "$nbThread"
    done
  done
done

python3.11 compute_avg_throughput.py -t ConcurrentHashMap -typeOp ALL -p microbenchmark_results/avg_perf/64k -u 1000 -d
python3.11 compute_avg_throughput.py -t ExtendedSegmentedHashMap -typeOp ALL -p microbenchmark_results/avg_perf/64k -u 1000 -d

python3.11 generate_histograme_latex_size.py -od ExtendedSegmentedHashMap -oj ConcurrentHashMap -s 16k -s 32k -s 64k

./run_mining.sh

cd results
pdflatex -interaction=nonstopmode all_figures.tex > /dev/null
cd ..