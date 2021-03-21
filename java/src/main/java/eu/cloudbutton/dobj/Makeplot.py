import os
import matplotlib.pyplot as plt
import numpy as np
import sys

files = []
for arg in sys.argv[1:]:
    files.append(open(arg,"r"))

for i, l in enumerate(files[0]):
    pass

numprocess = []

for j in range(i):
    numprocess.append(j)

resultats = []

for file in files:
    resultats.append([int(float(i)) for i in file.read().split()[:len(numprocess)]])

print (resultats)

for resultat, name in zip(resultats,sys.argv[1:]):
    plt.plot(numprocess, resultat, marker = "o", label=name[9:len(name)-4].replace("_", " "))

plt.ylabel("ope/s")
plt.xlabel("# processes")
plt.legend(bbox_to_anchor=(0., 1.02, 1., .102), loc='lower left', ncol=2, mode="expand", borderaxespad=0.)
#plt.yscale("log")
plt.show()
