#!/usr/bin/env bash

export CLASSPATH="target/degradableobject-1.0.jar:target/degradableobject-1.0-tests.jar:target/lib/*"

type=$1
time=$2
nbThreads=$3
nbOps=$4

java -Xlog:gc -XX:+UseNUMA -XX:+UseG1GC -XX:-RestrictContended -ea --enable-preview \
eu.cloudbutton.dobj.benchmark.Microbenchmark \
-type $type \
-nbThreads $nbThreads \
-time $time \
-nbOps $nbOps \
-quickTest \
-p