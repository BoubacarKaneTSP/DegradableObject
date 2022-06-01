#!/bin/bash

typeCounter=""
typeSet=""
typeQueue=""
typeList=""
typeMap=""
typeTest=""
ratio="100 0 0"
print=""
save=""
workloadTime=5
warmingUpTime=1
nbTest=1
type=""
printFail=""
asymmetric=""
collisionKey=""
quickTest=""

while getopts 'c:s:q:l:m:t:r:pew:u:n:fakvxo' OPTION; do
  case "$OPTION" in
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

      if [[ $typeTest == "Benchmark" ]]
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
            echo "One type must be specified for the Benchmark. (Before test specification)" >&2
            exit 1
          fi

      elif [[ $typeTest == "Retwis" ]]
      then
        if [[ $typeCounter == "" ]]
          then
            typeCounter=Counter
          fi

          if [[ $typeSet == "" ]]
          then
            typeSet=Set
          fi

          if [[ $typeList == "" ]]
          then
            typeList=List
          fi

          if [[ $typeQueue == "" ]]
          then
            typeQueue=Queue
          fi

          if [[ $typeMap == "" ]]
          then
            typeMap=Map
          fi
        typeCounter=Counter
        typeSet=Set
        typeQueue=Queue
        typeList=List
        typeMap=Map
      else
        echo "Test type must be Benchmark or Retwis." >&2
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
      workloadTime="$OPTARG"
      ;;
    u)
      warmingUpTime="$OPTARG"
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
    x)
      mvn clean package -f ../java -DskipTests;
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
      [-p] print,
      [-e] save,
      [-w] workload Time in sec,
      [-u] warming up Time in sec,
      [-n] number of test
      [-f] print the ratio of operations that failed
      [-a] test an asymmetrical workload
      [-k] test the map with collision on key
      [-v] testing only one and max nbThreads" >&2
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
      [-w] workload Time in sec,
      [-u] warming up Time in sec,
      [-n] number of test
      [-f] print the ratio of operations that failed
      [-a] test an asymmetrical workload
      [-k] test the map with collision on key
      [-v] testing only one and max nbThreads" >&2
      exit 1
      ;;
  esac
done

echo "The counter used is : $typeCounter"
echo "The set used is : $typeSet"
echo "The queue used is : $typeQueue"
echo "The list used is : $typeList"
echo "The map used is : $typeMap"
echo "The test launched is : $typeTest"
echo "The ratio of write is : $ratio"
echo "The workload time is : $workloadTime"
echo "The warming up time is : $warmingUpTime"
echo "The number of test is : $nbTest"

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

if [[ $typeTest == "Benchmark" ]]
then
  CLASSPATH=../java/target/*:../java/target/lib/* numactl -N 0 -m 0 java -XX:+UseNUMA -XX:+UseG1GC eu.cloudbutton.dobj.Benchmark.Benchmark -type $type -ratios $ratio -nbTest $nbTest -time $workloadTime -wTime $warmingUpTime $print $save $printFail $asymmetric $collisionKey $quickTest
fi