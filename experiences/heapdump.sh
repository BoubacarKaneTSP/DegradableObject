#!/bin/bash

# kill all the children of the current process
#trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
#trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

rm heapdump_${1}_${2}.hprof

tail -F heapdump_$1_$2.txt |
grep --line-buffered 'heap dump' |
while read ;
do
  jcmd $3 GC.heap_dump heapdump_$1_$2.hprof
  kill $$
  break
done