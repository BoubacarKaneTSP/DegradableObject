#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

cd ~/IdeaProjects/DegradableObject/java/
ls
for type in 'Counter' 'DegradableCounter' 'List' 'DegradableList' 'Set' 'DegradableSet'
do
  (mvn clean package -DskipTests; CLASSPATH=target/*:target/lib/* java  eu.cloudbutton.dobj.Benchmark -type $type -ratios 0)
done