#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

./test.sh -c CounterJUC -s ConcurrentSkipListSet -q ConcurrentLinkedQueue -m ConcurrentHashMap -t Retwis -p -e -w 30 -u 10 -n 10 -b -h "JUC"
./test.sh -c CounterJUC -s ConcurrentSkipListSet -q ConcurrentLinkedQueue -m MapAddIntensive -t Retwis -p -e -w 30 -u 10 -n 10 -b -h "M"
./test.sh -c CounterJUC -s SetAddIntensive -q ConcurrentLinkedQueue -m MapAddIntensive -t Retwis -p -e -w 30 -u 10 -n 10 -b -h "M_S"
./test.sh -c CounterJUC -s SetAddIntensive -q QueueMASP -m MapAddIntensive -t Retwis -p -e -w 30 -u 10 -n 10 -b -h "Q_M_S"
./test.sh -c CounterIncrementOnly -s SetAddIntensive -q QueueMASP -m MapAddIntensive -t Retwis -p -e -w 30 -u 10 -n 10 -b -h "Q_M_S_C"

#./test.sh -c CounterJUC -s ConcurrentSkipListSet -q ConcurrentLinkedQueue -m ConcurrentHashMap -t Retwis -p -e -w 30 -u 10 -n 10 -z -h "JUC_CompletionTime"
#./test.sh -c CounterJUC -s ConcurrentSkipListSet -q QueueMASP -m ConcurrentHashMap -t Retwis -p -e -w 30 -u 10 -n 10 -z -h "Q_CompletionTime"
#./test.sh -c CounterJUC -s ConcurrentSkipListSet -q QueueMASP -m MapAddIntensive -t Retwis -p -e  -w 30 -u 10 -n 10 -z -h "Q_M_CompletionTime"
#./test.sh -c CounterJUC -s SetAddIntensive -q QueueMASP -m MapAddIntensive -t Retwis -p -e -w 30 -u 10 -n 10 -z -h "Q_M_S_CompletionTime"
#./test.sh -c CounterIncrementOnly -s SetAddIntensive -q QueueMASP -m MapAddIntensive -t Retwis -p -e -w 30 -u 10 -n 10 -z -h "Q_M_S_C_CompletionTime"
