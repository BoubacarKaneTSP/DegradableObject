import sys
import os

type_obj = sys.argv[1]

list_op = ["ALL", "ADD", "REMOVE" ,"READ"]

for op in list_op:
    os.remove(type_obj + "_" + op + ".txt")
