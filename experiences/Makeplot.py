import os
import matplotlib.pyplot as plt
import numpy as np
import sys

files = []
for arg in sys.argv[1:]:
    print(arg)
    files.append(open(arg,"r"))

resultats = list()
numprocesses = list()
i = 0

for file in files:
    numprocess = []
    resultat = []
    for elt in file.read().split():
        #print(elt)
        if i % 2 == 0:
            numprocess.append(elt)
        else:
            resultat.append(int(float(elt)))
        i += 1
    resultats.append(resultat)
    numprocesses.append(numprocess)

print(resultats)
print(numprocesses)

for numprocess, resultat, name in zip(numprocesses,resultats,sys.argv[1:]):
    plt.plot(numprocess, resultat, marker = "o", label=name[7:len(name)-4].replace("_", " "))


plt.ylabel("# ope/s")
plt.xlabel("# processes")
plt.legend(bbox_to_anchor=(0., 1.02, 1., .102), loc='lower left', ncol=2, mode="expand", borderaxespad=0.)
plt.yscale("log")
plt.show()
