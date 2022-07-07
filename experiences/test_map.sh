
for nbInitialAdd in 10 100 1000 10000 100000 1000000 10000000
do
CLASSPATH=../java/target/*:../java/target/lib/* numactl -N 0 -m 0 java -XX:+UseNUMA -XX:+UseG1GC eu.cloudbutton.dobj.Benchmark.Benchmark -type Map -ratios "100 0 0" -nbTest 5 -time 20 -wTime 10 -nbOps $nbInitialAdd -p -s -asymmetric -collisionKey >> Map_1_thread.txt
done