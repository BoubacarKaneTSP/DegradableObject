import os
import time
from factories import ListFactory

obj = ListFactory().create_list("RList","redistestlist"+str(os.getpid()))

NBOPERATION = 10000
NBOPERATION_p = 0

START = time.time()

while NBOPERATION_p < NBOPERATION:

    obj.append(str(NBOPERATION_p))
    NBOPERATION_p = NBOPERATION_p + 1

EXECTIME = time.time() - START

OPESECONDE = NBOPERATION / EXECTIME

#print(len(obj.read()))
#print(str(EXECTIME))
print(str(OPESECONDE))
