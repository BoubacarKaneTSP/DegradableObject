import sys
import os

type_obj = sys.argv[1]

list_op = ["ALL", "ADD", "REMOVE" ,"READ"]

for op in list_op:
    os.remove(op+"_"+type_obj + ".txt")
