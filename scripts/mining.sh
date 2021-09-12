#!/bin/bash

TMPDIR=/tmp

clazzes="ConcurrentSkipListSet"
repo="https://github.com/apache/ignite"

for r in ${repo}
do
    software=$(echo ${r} | awk -F"/" '{print $NF}')
    git clone ${r} ${TMPDIR}/${software}
    for clazz in ${clazzes}
    do
	files=$(find ${TMPDIR}/${software} -iname "*.java" | xargs egrep "[a-z]+ = new ${clazz}" $1 | awk '{print $1}' | sed s/://g)
	for f in ${files}
	do
	    var=$(egrep "[a-z]+ = new ${clazz}" $f | awk -F"=" '{print $1}' | awk '{print $NF}' | sed s/this\.//g | xargs echo -n)
	    isPrivate=$(grep ${var} ${f} | grep private) # FIXME    
	    if [ "${isPrivate}" != "" ];
	    then
		isPrivate="Y"
	    else
		isPrivate="N"
	    fi
	    c=$(echo ${f} | awk -F"/" '{print $NF}' | sed s/\\.java//g)
	    u=$(grep "${var}\." $(find ${TMPDIR}/${software} -iname ${c}.java) | sed "s/.*${var}\.\([a-zA-Z]*\).*/\1/g" | sort | uniq | rs -T)
	    echo ${software}" "${clazz}" "${c}" "${isPrivate}" "${u}
	done
    done
    rm -Rf ${TMPDIR}/${software}
done



