#!/usr/bin/env bash
CLASSPATH=target/degradableobject-1.0.jar:target/degradableobject-1.0-tests.jar:target/lib/* java -XX:+UseNUMA -XX:+UseG1GC  eu.cloudbutton.dobj.Benchmark -type java.util.concurrent.ConcurrentLinkedQueue -ratios 100 0 -time 10 -nbThreads 1
