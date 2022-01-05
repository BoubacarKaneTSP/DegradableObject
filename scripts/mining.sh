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
		var=$(grep -oP "[[:alpha:]]+ = new ${clazz}" $f | awk -F"=" '{print $1}' | awk '{print $NF}' | head -n 1) # FIXME multiple declaration on the same line ..

		isPrivate=$(grep ${var} ${f} | grep private) # FIXME
		if [ "${isPrivate}" != "" ];
		then
		    isPrivate="Y"
		else
		    isPrivate="N"
		fi
		c=$(echo ${f} | awk -F"/" '{print $NF}' | sed s/\\.java//g)
		u=$(grep "${var}\." $(find ${TMPDIR}/${software} -iname ${c}.java) | sed "s/.*${var}\.\([a-zA-Z]*\).*/\1/g" | sort | uniq | rs -T)
		r=()
		for m in ${u};
		do
		    me=""
		    if [ "$(find ${TMPDIR}/${software} -iname ${c}.java | xargs grep -P "(return[[:blank:]]*${var}\.${m}\(.*\))|(\=[[:blank:]]*${var}\.${m}\(.*\))|(\(${var}\.${m}\(.*\))" $1)" != "" ];
		    then
			me+="+"
		    fi
		    me+=${m}
		    r+=(${me})
		done
		echo ${software}" "${clazz}" "${c}" "${isPrivate}" "${r[@]}
	    fi
	done
    done
    rm -Rf ${TMPDIR}/${software}
done

