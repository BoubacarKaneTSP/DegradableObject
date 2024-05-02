#!/bin/bash

# kill all the children of the current process
# trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
# trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

typeCounter=""
typeSet=""
typeQueue=""
typeList=""
typeMap=""
typeTest=""
ratio="100 0 0"
distribution="0 10 35 55"
print=""
save=""
completionTime=""
nbUserInit=""
nbUser=""
workloadTime=""
warmingUpTime=""
nbTest=1
type=""
printFail=""
asymmetric=""
collisionKey=""
quickTest=""
nbInitialAdd=""
breakdown=""
tag=""
nbThreads=""
nbItemsPerThread=""
computeGCInfo=false
alpha=""
compile=false

while getopts 'xc:s:q:l:m:t:r:pew:u:n:fakvoi:zy:bh:g:d:jA:' OPTION; do
    case "$OPTION" in
	x)
	    mvn clean package -f ../java -DskipTests;
	    compile=true
	    ;;
	A)
	    alpha="$OPTARG"
	    ;;
	c)
	    typeCounter="$OPTARG"
	    ;;
	s)
	    typeSet="$OPTARG"
	    ;;
	q)
	    typeQueue="$OPTARG"
	    ;;
	l)
	    typeList="$OPTARG"
	    ;;
	m)
	    typeMap="$OPTARG"
	    ;;
	t)
	    typeTest="$OPTARG"

	    if [[ $typeTest == "Microbenchmark" ]]
	    then
		sum=0
		if [[ $typeCounter != "" ]]
		then
		    ((sum+=1))
		    type=$typeCounter
		fi

		if [[ $typeSet != "" ]]
		then
		    ((sum+=1))
		    type=$typeSet
		fi

		if [[ $typeList != "" ]]
		then
		    ((sum+=1))
		    type=$typeList
		fi

		if [[ $typeQueue != "" ]]
		then
		    ((sum+=1))
		    type=$typeQueue
		fi

		if [[ $typeMap != "" ]]
		then
		    ((sum+=1))
		    type=$typeMap
		fi

		if [[ ! ($sum -eq 1) ]]
		then
		    echo "One type must be specified in order to run the micro-Benchmark. (Before test specification)" >&2
		    exit 1
		fi

		echo "The type being tested is : $type"

	    elif [[ $typeTest == "Retwis" ]]
	    then
		if [[ $typeCounter == "" ]] && [[ $typeSet == "" ]] && [[ $typeQueue == "" ]] && [[ $typeMap == "" ]]
		then
		    echo "Must be specified a type for : a Counter, a Set, a Queue and a Map in order to run the Retwis Benchmark (Before test specification)" >&2
		    exit 1
		fi
		echo "The counter used is : $typeCounter"
		echo "The set used is : $typeSet"
		echo "The queue used is : $typeQueue"
		echo "The map used is : $typeMap"
	    else
		echo "Test type must be Microbenchmark or Retwis." >&2
		exit 1
	    fi
	    ;;
	r)
	    ratio="$OPTARG"
	    ;;
	p)
	    print="-p"
	    ;;
	e)
	    save="-s"
	    ;;
	w)
	    workloadTime="-time $OPTARG"
	    ;;
	u)
	    warmingUpTime="-wTime $OPTARG"
	    ;;
	n)
	    nbTest="$OPTARG"
	    ;;
	f)
	    printFail="-ratioFail"
	    ;;
	a)
	    asymmetric="-asymmetric"
	    ;;
	k)
	    collisionKey="-collisionKey"
	    ;;
	v)
	    quickTest="-quickTest"
	    ;;
	i)
	    nbInitialAdd="-nbOps $OPTARG"
	    ;;
	z)
	    completionTime="-completionTime"
	    ;;
	y)
	    nbUserInit="-nbUserInit $OPTARG"
	    nbUser="$OPTARG"
	    ;;
	b)
	    breakdown="-breakdown"
	    ;;
	h)
	    tag="$OPTARG"
	    ;;
	g)
	    nbThreads="-nbThreads $OPTARG"
	    ;;
	d)
	    nbItemsPerThread="-nbItems $OPTARG"
	    ;;
	j)
	    computeGCInfo=true
	    ;;
	o)
	    echo "script usage: $(basename \$0)
      [-a] Test an asymmetrical workload,
      [-b] Print the details results for all operations,
      [-c] counter type,
      [-d] Number of items max added per thread,
      [-e] save,
      [-f] Print the ratio of operations that failed,
      [-g] nbThreads computed,
      [-h] Tag associated with the name of the cpuIDs file,
      [-i] Number of object initially added,
      [-j] Compute time spent doing GC,
      [-k] Test the map with collision on key,
      [-l] list type,
      [-m] map type,
      [-n] Number of test,
      [-o] show options,
      [-p] print,
      [-q] queue type,
      [-r] ratio of each operations in %,
      [-s] set type,
      [-t] test type,
      [-u] Warming up Time in sec,
      [-v] Testing only one and max nbThreads,
      [-w] Workload Time in sec,
      [-x] compile the project,
      [-y] Number of initial user in Retwis,
      [-z] Computing the completionTime for Retwis">&2
	    exit 1
	    ;;
	?)
	echo "script usage: $(basename \$0)
      [-a] Test an asymmetrical workload,
      [-b] Print the details results for all operations,
      [-c] counter type,
      [-d] Number of items max added per thread,
      [-e] save,
      [-f] Print the ratio of operations that failed,
      [-g] nbThreads computed,
      [-h] Tag associated with the name of the cpuIDs file,
      [-i] Number of object initially added,
      [-j] Compute time spent doing GC,
      [-k] Test the map with collision on key,
      [-l] list type,
      [-m] map type,
      [-n] Number of test,
      [-o] show options,
      [-p] print,
      [-q] queue type,
      [-r] ratio of each operations in %,
      [-s] set type,
      [-t] test type,
      [-u] Warming up Time in sec,
      [-v] Testing only one and max nbThreads,
      [-w] Workload Time in sec,
      [-x] compile the project,
      [-y] Number of initial user in Retwis,
      [-z] Computing the completionTime for Retwis">&2
	exit 1
	;;
    esac
done

cpuIDs=""
ranges=("0-19" "80-99" "20-39" "100-119" "40-59" "120-139" "60-79" "140-159")

nthreads=$(echo "$nbThreads" | grep -o '[0-9]\+')
echo "nbThreads => $nthreads"
nhwthreads=$(cat /proc/cpuinfo  | grep processor | tail -n 1 | awk '{print ($3)+1}')
echo "nhwthreads => $nhwthreads"
min=$(echo -e ${nthreads}"\n"${nhwthreads} | sort -n | head -n 1)
echo ${min}

val=0

if [ "$compile" = false ]; then
    for range in "${ranges[@]}"; do
	start=$(echo "$range" | cut -d'-' -f1)
	end=$(echo "$range" | cut -d'-' -f2)
	for ((i=start; i<=end; i++)); do
            if [ "$val" -lt "$min" ]; then
		if [ -n "$cpuIDs" ]; then
		    cpuIDs="$cpuIDs,$i"
		else
		    cpuIDs="$i"
		fi
            fi
            ((val += 1))
	done
    done
fi

echo "The test launched is : $typeTest"
echo "The ratio of write is : $ratio (ADD,FOLLOW/UNFOLLOW,TWEET,READ,GROUP,PROFILE)"
echo "The workload time is : $workloadTime"
echo "The warming up time is : $warmingUpTime"
echo "The number of test is : $nbTest"
echo "Number of object initially added : $nbInitialAdd"
echo "The number of threads is : $nbThreads"
echo "Status of collisionKey : $collisionKey"
echo "cpuIDs: $cpuIDs"
echo ""

#cpuIDs=""
#var=$(echo "$nbThreads" | grep -o '[0-9]\+')
#
## shellcheck disable=SC2004
#for ((i=0; i<$(($var)); i++)); do
#  if [ -n "$cpuIDs" ]; then
#    cpuIDs="$cpuIDs,$i"
#  else
#    cpuIDs="$i"
#  fi
#done

# Xlog:gc
JVM_EXPORTS="--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/jdk.internal.misc=ALL-UNNAMED --add-exports java.base/jdk.internal.access=ALL-UNNAMED --add-exports java.base/jdk.internal.util.random=ALL-UNNAMED"
JVM_ARGS="-Xms5g -Xmx16g -XX:+UseNUMA -XX:+UseG1GC -XX:-RestrictContended -ea ${JVM_EXPORTS}"
# JVM_ARGS="-Xms5g -Xmx16g -XX:+UseNUMA -XX:+UseG1GC -XX:-RestrictContended ${JVM_EXPORTS}"

RETWIS_ARGS="-set $typeSet -queue $typeQueue -counter $typeCounter -map $typeMap -distribution $ratio -nbTest $nbTest $nbThreads $workloadTime $warmingUpTime $nbInitialAdd $completionTime $nbUserInit $print $save $breakdown $quickTest $collisionKey $nbItemsPerThread -tag $tag -gcinfo -alphaMin ${alpha} -generate"

NUMA="numactl --physcpubind=$cpuIDs --membind=0"

if [[ $typeTest == "Microbenchmark" ]]
then
    if [[ $computeGCInfo == true ]]
    then
	CLASSPATH=../java/target/*:../java/target/lib/* numactl --physcpubind="$cpuIDs" --membind=0 java ${JVM_ARGS} eu.cloudbutton.dobj.benchmark.Microbenchmark -type $type -ratios $ratio -nbTest $nbTest $nbThreads $workloadTime $warmingUpTime $nbInitialAdd $print $save $printFail $asymmetric $collisionKey $quickTest $nbItemsPerThread -gcinfo | egrep "nbThread|benchmarkAvgTime|Start benchmark|End benchmark|G1 Evacuation Pause" > "$type"_gcinfo.log
	python3 analyse_gc.py $type $nbTest "$nbUserInit"
    else
	CLASSPATH=../java/target/*:../java/target/lib/* numactl --physcpubind="$cpuIDs" --membind=0 java ${JVM_ARGS} eu.cloudbutton.dobj.benchmark.Microbenchmark -type $type -ratios $ratio -nbTest $nbTest $nbThreads $workloadTime $warmingUpTime $nbInitialAdd $print $save $printFail $asymmetric $collisionKey $quickTest $nbItemsPerThread
    fi
elif [[ $typeTest == "Retwis" ]]
then
    if [[ $computeGCInfo == true ]]
    then
	CLASSPATH=../java/target/*:../java/target/lib/* ${NUMA} java ${JVM_ARGS} eu.cloudbutton.dobj.benchmark.Retwis ${RETWIS_ARGS} | egrep "nbThread|benchmarkAvgTime|Start benchmark|End benchmark|G1 Evacuation Pause" > "$tag"_gcinfo.log
	python3 analyse_gc.py $tag $nbTest $nbUserInit
    else
	CLASSPATH=../java/target/*:../java/target/lib/* ${NUMA} java ${JVM_ARGS} eu.cloudbutton.dobj.benchmark.Retwis ${RETWIS_ARGS}
	echo ${cmd}
	eval ${cmd}
    fi
fi
