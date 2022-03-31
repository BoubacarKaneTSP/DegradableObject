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
        if i % 2 == 0:
            numprocess.append(elt)
        else:
            print(float(elt))
            resultat.append(float(elt))
        i += 1
    resultats.append(resultat)
    numprocesses.append(numprocess)

#print(resultats)
#print(numprocesses)

for numprocess, resultat, name in zip(numprocesses,resultats,sys.argv[1:]):
    plt.plot(numprocess, resultat, marker = "o", linewidth=2, markersize=12, label=name[:len(name)-4].replace("_", " "))


SIZE = 15

plt.rcParams.update({'font.size': SIZE})
plt.xticks(fontsize=SIZE)
plt.yticks(fontsize=SIZE)


plt.gca().set_ylim([10000,10000000])
# plt.gca().set_ylim([0.00000001,0.001])
# plt.gca().invert_yaxis()
plt.gca().spines['right'].set_visible(False)
plt.gca().spines['top'].set_visible(False)
plt.gca().tick_params(axis='both', which='major', labelsize=SIZE, length=SIZE)
plt.gca().tick_params(axis='both', which='minor', labelsize=SIZE, length=SIZE)
plt.ylabel("throughput", fontsize=SIZE)
plt.xlabel("# processes", fontsize=SIZE)
plt.legend(bbox_to_anchor=(0., 1.04, 1., 1.), loc='lower left', ncol=2, mode="expand", borderaxespad=0.)
plt.yscale("log")
plt.show()
