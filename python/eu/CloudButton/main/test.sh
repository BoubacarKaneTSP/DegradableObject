#!/bin/bash

# kill all the children of the current process
trap "pkill -KILL -P $$; exit 255" SIGINT SIGTERM
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

export PYTHONPATH="/home/bkane/Cassandra/DegradableObjects"

echo "${PYTHONPATH}"

rm Resultats/*

NBTEST="10"

for type in 'list' 'set' 'counter'
do
	echo " " > "resultat_${type}.txt"

	for i in 1 2 3 4 5 6 7 8 9 10 12 14 16 18 20 25 30 40;
	#for i in 1 5 10;
	do
		echo " " > "Resultats/resultats_${type}_${i}_process.txt"
		for j in `seq 1 $NBTEST`
		do
			echo "Lancement du workload $type pour *$i* processus (test numero $j)"
			python3 reset.py

			for process in `seq 1 $i`
			do
				echo "* lancement process $process *"
				python3 workloads/workload_${type}.py >> "Resultats/resultats_${type}_${i}_process.txt" &
			done

			wait
			echo "fin"

		done

		python3 read_results.py $i $NBTEST $type >> "resultat_${type}.txt"

	done
done
echo " Fin du programme "
