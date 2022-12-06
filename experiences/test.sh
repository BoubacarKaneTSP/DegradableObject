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
distribution="0 0 0 100"
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

while getopts 'xc:s:q:l:m:t:r:d:pew:u:n:fakvoi:zybh:g:' OPTION; do
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
    d)
      distribution="$OPTARG"
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
    z)
      multipleOperation="-multipleOperation"
      ;;
    b)
      breakdown="-breakdown"
      ;;
    h)
      tag="-tag $OPTARG"
      ;;
    g)
      nbThreads="-nbThreads $OPTARG"
      ;;
    o)
      echo "script usage: $(basename \$0)
      [-c] counter type,
      [-s] set type,
      [-q] queue type,
      [-l] list type,
      [-m] map type,
      [-t] test type,
      [-r] ratio of write in %,
      [-d] distribution of operations in Retwis
      [-p] print,
      [-e] save,
      [-w] workload Time in sec,
      [-u] warming up Time in sec,
      [-n] number of test,
      [-f] print the ratio of operations that failed,
      [-a] test an asymmetrical workload,
      [-k] test the map with collision on key,
      [-i] Number of object initially added,
      [-v] testing only one and max nbThreads,
      [-z] Computing the completionTime for Retwis,
      [-y] Computing multiple time the same operation for Retwis,
      [-b] Print the details results for all operations
      [-h] Tag associated with the name of the result file,
      [-g] nbThreads computed">&2
      exit 1
      ;;
    ?)
      echo "script usage: $(basename \$0)
      [-c] counter type,
      [-s] set type,
      [-q] queue type,
      [-l] list type,
      [-m] map type,
      [-t] test type,
      [-r] ratio of write in %,
      [-p] print,
      [-e] save,
      [-w] Workload Time in sec,
      [-u] Warming up Time in sec,
      [-n] Number of test,
      [-f] Print the ratio of operations that failed,
      [-a] Test an asymmetrical workload,
      [-k] Test the map with collision on key,
      [-i] Number of object initially added,
      [-v] Testing only one and max nbThreads,
      [-z] Computing the completionTime for Retwis,
      [-y] Computing multiple time the same operation for Retwis,
      [-b] Print the details results for all operations
      [-h] Tag associated with the name of the result file,
      [-g] nbThreads computed">&2
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

  #CLASSPATH=../java/target/*:../java/target/lib/* numactl -N 0 -m 0 java -XX:+UseNUMA -XX:+UseG1GC -verbose:gc eu.cloudbutton.dobj.cenchmark.Microbenchmark -type $type -ratios $ratio -nbTest $nbTest -time $workloadTime -wTime $warmingUpTime $print $save $printFail $asymmetric $collisionKey $quickTest
  CLASSPATH=../java/target/*:../java/target/lib/* numactl -N 0 -m 0 java -Xlog:gc -XX:+UseNUMA -XX:+UseG1GC -XX:-RestrictContended eu.cloudbutton.dobj.benchmark.Microbenchmark -type $type -ratios $ratio -nbTest $nbTest $workloadTime $warmingUpTime $nbInitialAdd $print $save $printFail $asymmetric $collisionKey $quickTest
elif [[ $typeTest == "Retwis" ]]
then
  CLASSPATH=../java/target/*:../java/target/lib/* numactl -N 0 -m 0 java -XX:+UseNUMA -XX:+UseG1GC -XX:-RestrictContended eu.cloudbutton.dobj.benchmark.Retwis -set $typeSet -queue $typeQueue -counter $typeCounter -map $typeMap -distribution $distribution -nbTest $nbTest $nbThreads $workloadTime $warmingUpTime $nbInitialAdd $completionTime $multipleOperation $print $save $breakdown $quickTest $tag
fi
