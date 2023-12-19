import os
import matplotlib.pyplot as plt
import numpy as np
import sys
import matplotlib.font_manager as fm

files = []
linestyle_tuple = ['-', '--', '-.', ':', 'solid', 'dashed', 'dashdot', 'dotted']
linestyles = ['solid', 'dotted', 'dashdot', 'dashed']
markers = ["o", "s", "*", "P", "d", "v", "<", ">", "^", "p", "D", "1", "h", "X"]
font = {'fontname': 'Arial'}

plt.rcParams.update({"font.family": "Arial"})

start = 1
op = sys.argv[1]

if len(sys.argv) > 2 :
    nb_user = sys.argv[2]
    nameFile = op + "_" + nb_user
else:
    nameFile = sys.argv[1]

nameFiles = sys.argv[1:]

if ".txt" not in nameFile:
    nameFiles = sys.argv[3:]
    start = 3

for arg in sys.argv[start:]:
    print(arg)
    files.append(open(arg, "r"))

resultats = list()
numprocesses = list()
i = 0

for file in files:
    numprocess = []
    resultat = []
    # print(file.read().split("\n"))

    for elt in file.read().split("\n"):
        elt = elt.split()

        if len(elt) != 0:
            numprocess.append(elt[0])
            resultat.append(float(elt[1]))
        print(elt)

    resultats.append(resultat)
    numprocesses.append(numprocess)

# print(resultats)
# print(numprocesses)
labels = []
labels.append('$S_1$')
labels.append('$S_2$')

f = plt.figure()
f.set_figwidth(15)
f.set_figheight(10)

for numprocess, resultat, name, linestyle, marker in zip(numprocesses, resultats, nameFiles, linestyle_tuple, markers):
    list_split_label = name.split("/")
    label = list_split_label.pop()
    label = label[:len(label) - 4].replace("_", " ")

    # int_resultat = []
    # for res in resultat:
    #     int_resultat.append(int(res)/1000000000)


    plt.plot(numprocess, resultat, marker=marker, linestyle=linestyle, linewidth=2, markersize=12, label=label)
    # plt.hist(numprocess, resultat, label=label)

SIZE = 25

plt.rcParams.update({'font.size': SIZE})
plt.xticks(fontsize=SIZE)
plt.yticks(fontsize=SIZE)

# plt.ticklabel_format(style='sci', axis='y', scilimits=(9,9))

# plt.gca().set_ylim([100000, 100000000])
# plt.gca().set_ylim([0.00000001,0.001])
# plt.gca().set_ylim([-10, 100])
# plt.gca().invert_yaxis()
plt.gca().spines['right'].set_visible(False)
plt.gca().spines['top'].set_visible(False)
plt.gca().tick_params(axis='both', which='major', labelsize=SIZE, length=SIZE)
plt.gca().tick_params(axis='both', which='minor', labelsize=SIZE, length=SIZE)
plt.gca().yaxis.get_offset_text().set_fontsize(SIZE)
plt.gca().yaxis.get_offset_text().set_position((-0.05,0))
# plt.ylabel("(%)", fontsize=SIZE)
plt.ylabel("NB request", fontsize=SIZE)
plt.xlabel("# processes", fontsize=SIZE)
plt.legend(bbox_to_anchor=(0., 1.02, 1., .102), loc='lower left', ncol=1, mode="expand", borderaxespad=0.,labelspacing = 1, prop={'size': SIZE})
# plt.yscale("log")



plt.tight_layout()
if ".txt" not in nameFile:
    plt.savefig("figures/"+nb_user+"_users/"+op+".png")
else:
    plt.show()