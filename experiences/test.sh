#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

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
multipleOperation=""
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

while getopts 'xc:s:q:l:m:t:r:pew:u:n:fakvoi:zybh:g:d:j' OPTION; do
  case "$OPTION" in
    x)
      mvn clean package -f ../java -DskipTests;
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
      multipleOperation="-multipleOperation"
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
      [-h] Tag associated with the name of the result file,
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
      [-y] Computing multiple time the same operation for Retwis,
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
      [-h] Tag associated with the name of the result file,
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
      [-y] Computing multiple time the same operation for Retwis,
      [-z] Computing the completionTime for Retwis">&2
      exit 1
      ;;
  esac
done

echo "The test launched is : $typeTest"
echo "The ratio of write is : $ratio"
echo "The workload time is : $workloadTime"
echo "The warming up time is : $warmingUpTime"
echo "The number of test is : $nbTest"
echo "Number of object initially added : $nbInitialAdd"
echo "Status of collisionKey : $collisionKey"
echo "Status of multipleOperation : $multipleOperation"

if [[ $typeTest == "Microbenchmark" ]]
then
  if [[ $computeGCInfo == true ]]
  then
  CLASSPATH=../java/target/*:../java/target/lib/* numactl -N 0 -m 0 java -Xlog:gc -XX:+UseNUMA -XX:+UseG1GC -XX:-RestrictContended eu.cloudbutton.dobj.benchmark.Microbenchmark -type $type -ratios $ratio -nbTest $nbTest $workloadTime $warmingUpTime $nbInitialAdd $print $save $printFail $asymmetric $collisionKey $quickTest $nbItemsPerThread -gcinfo | egrep "nbThread|benchmarkAvgTime|Start benchmark|End benchmark|G1 Evacuation Pause" > "$type"_gcinfo.log
  python3 analyse_gc.py $type $nbTest
  else
  CLASSPATH=../java/target/*:../java/target/lib/* numactl -N 0 -m 0 java -XX:+UseNUMA -XX:+UseG1GC -XX:-RestrictContended eu.cloudbutton.dobj.benchmark.Microbenchmark -type $type -ratios $ratio -nbTest $nbTest $workloadTime $warmingUpTime $nbInitialAdd $print $save $printFail $asymmetric $collisionKey $quickTest $nbItemsPerThread
  fi
elif [[ $typeTest == "Retwis" ]]
then
  if [[ $computeGCInfo == true ]]
  then
  CLASSPATH=../java/target/*:../java/target/lib/* numactl -N 0 -m 0 java -Xms5g -Xmx20g -XX:+UseNUMA -XX:+UseG1GC -XX:-RestrictContended -ea eu.cloudbutton.dobj.benchmark.Retwis -set $typeSet -queue $typeQueue -counter $typeCounter -map $typeMap -distribution $ratio -nbTest $nbTest $nbThreads $workloadTime $warmingUpTime $nbInitialAdd $completionTime $multipleOperation $print $save $breakdown $quickTest $collisionKey $nbItemsPerThread -tag $tag -gcinfo #| egrep "nbThread|benchmarkAvgTime|Start benchmark|End benchmark|G1 Evacuation Pause" > "$tag"_gcinfo.log
#  python3 analyse_gc.py $tag $nbTest
  else
  CLASSPATH=../java/target/*:../java/target/lib/* numactl -N 0 -m 0 java -XX:+UseNUMA -XX:+UseG1GC -XX:-RestrictContended -ea eu.cloudbutton.dobj.benchmark.Retwis -set $typeSet -queue $typeQueue -counter $typeCounter -map $typeMap -distribution $ratio -nbTest $nbTest $nbThreads $workloadTime $warmingUpTime $nbInitialAdd $completionTime $multipleOperation $print $save $breakdown $quickTest $collisionKey $nbItemsPerThread -tag $tag
  fi
fi
