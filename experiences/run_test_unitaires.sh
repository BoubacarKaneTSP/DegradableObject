#!/bin/bash

save=""
print=""
compile=""
workloadTime=10
warmingUpTime=5
numberOfTest=1

while getopts 'oepxw:u:n:' OPTION; do
	case "$OPTION" in
		o)
			echo "script usage: $(basename \$0)
			[-o] print option,
			[-e] save results,
			[-p] print results,
			[-x] compile the project
			[-w] workload time in seconds
			[-u] warming up time in seconds
			[-n] number of test" >&2
			exit 1
			;;
		e)
			save="-e "
			;;
		p)
			print="-p "
			;;
		x)
			compile="-x "
			;;
		w)
			workloadTime="$OPTARG"
			;;
		u)
			warmingUpTime="$OPTARG"
			;;
		n)
			numberOfTest="$OPTARG"
			;;
		?)
			echo "print the option with [-o]" >&2
			exit 1
			;;
	esac
done

for type in "Set" "DegradableSet" "ConcurrentHashSet" "Map" "DegradableMap" "Queue" "DegradableQueue" "DegradableList" "Counter" "DegradableCounter" "FuzzyCounter"
do
	if [[ $type == "Queue" || $type == "DegradableQueue" ]]
	then	
		./test.sh -c $type -t Benchmark -k -a -w $workloadTime -u $warmingUpTime -n $numberOfTest $print$compile$save
	elif [[ $type == "ConcurrentHashSet" || $type == "Set" || $type == "DegradableSet" ]]
	then
		./test.sh -c $type -t Benchmark -w $workloadTime -u $warmingUpTime -n $numberOfTest $print$compile$save
	else
		./test.sh -c $type -t Benchmark -k -w $workloadTime -u $warmingUpTime -n $numberOfTest $print$compile$save
	fi
done
