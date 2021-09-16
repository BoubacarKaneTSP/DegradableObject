#!/bin/bash

TMPDIR=/tmp

clazzes="ConcurrentSkipListSet ConcurrentLinkedQueue ConcurrentHashMap AtomicLong"
repo="https://github.com/apache/ignite https://github.com/apache/hadoop https://github.com/apache/cassandra"

for r in ${repo}
do
    software=$(echo ${r} | awk -F"/" '{print $NF}')
    git clone ${r} ${TMPDIR}/${software}
    files=$(find ${TMPDIR}/${software} -type f -iname "*.java")
    for f in ${files}
    do
	for clazz in ${clazzes}
	do
	    hit=$(egrep "[a-z]+ = new ${clazz}" ${f})
	    if [ "${hit}" != "" ];
	       then 
		   var=$(egrep "[a-z]+ = new ${clazz}" $f | awk -F"=" '{print $1}' | awk '{print $NF}' | sed s/this\.//g | head -n 1 | xargs echo -n) # FIXME
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
	    fi
	done
    done
    rm -Rf ${TMPDIR}/${software}
done



