find . -iname "*.java" | grep -iv test | xargs egrep "[a-z]+ = new ConcurrentLinkedQueue" $1 | awk -F"=" '{print $1}' | awk '{print "echo "$1"; grep \" "$NF"\\.\" "$1}' | sed s/://g | sh
