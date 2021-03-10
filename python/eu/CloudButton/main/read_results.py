import os
import sys
import numpy as np

def read_result(data_type,nb_process):
	resultat = open("Resultats/resultats_"+str(data_type)+"_"+str(nb_process)+"_process.txt","r")

	contenu = resultat.read()

	contenu = contenu.split("\n")
	
	return contenu[1:len(contenu)-1]
	

nb_process = int(sys.argv[1])
nb_test = int(sys.argv[2])
data_type = sys.argv[3]

results = []
tmp = []

tmp = read_result(data_type,nb_process)
for j in tmp:
	results.append(float(j))

array = np.array(results, dtype="int")

total = 0

for i in array:
	total += i
	
val = (total / (nb_test) )
print (val)
