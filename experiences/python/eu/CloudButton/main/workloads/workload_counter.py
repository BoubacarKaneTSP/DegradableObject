import os
import time

from python.eu.CloudButton.main.factories.counterfactory import CounterFactory

obj = CounterFactory().create_counter("RCounter", "test"+str(os.getpid()))

NBOPERATION = 10000
NBOPERATION_p = 0

START = time.time()

while NBOPERATION_p < NBOPERATION:

    obj.increment(1)
    NBOPERATION_p = NBOPERATION_p + 1

EXECTIME = time.time() - START

OPESECONDE = NBOPERATION / EXECTIME

#print(obj.read())
#print(str(EXECTIME))
print(str(OPESECONDE))
