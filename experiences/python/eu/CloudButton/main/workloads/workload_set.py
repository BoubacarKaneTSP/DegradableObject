import os
import time

from factories import SetFactory

obj = SetFactory().create_set("RSet","redistest"+str(os.getpid()))

NBOPERATION = 10000
NBOPERATION_p = 0

FLAG = START = time.time()

while NBOPERATION_p < NBOPERATION:

    obj.add(str(NBOPERATION_p)+str(os.getpid()))
    NBOPERATION_p = NBOPERATION_p + 1
    """
    if(NBOPERATION_p % 1000 == 0):
        print((time.time() - FLAG), NBOPERATION_p)
        FLAG = time.time()
    """
EXECTIME = time.time() - START

OPESECONDE = NBOPERATION / EXECTIME

#print(len(obj.read()))
#print(str(EXECTIME))
print(str(OPESECONDE))