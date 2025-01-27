import sys

import matplotlib.pyplot as plt

numprocess = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 14, 16, 18, 20, 25, 30, 40]
#numprocess = [1, 5, 10]


files = []
for arg in sys.argv[1:]:
    files.append(open(arg,"r"))

resultats = []

for file in files:
    resultats.append([int(float(i)) for i in file.read().split()[:len(numprocess)]])

print (resultats)

for resultat, name in zip(resultats,sys.argv[1:]):
    plt.plot(numprocess, resultat, marker = "o", label=name[7:len(name)-4].replace("_", " "))

plt.ylabel("ope/s")
plt.xlabel("# processes")
plt.legend(bbox_to_anchor=(0., 1.02, 1., .102), loc='lower left', ncol=2, mode="expand", borderaxespad=0.)
#plt.legend(bbox_to_anchor=(1.01, 0.5), loc='upper left', borderaxespad=0.)
#plt.yscale("log")
plt.show()

