import sys

object_name = sys.argv[1]
gcinfo_raw = open("gcinfo.log","r")
gcinfo = open(object_name + "_gc_usage.txt","w")
dico_gc = dict()

nb_test = int(sys.argv[2])

last_nb_thread = 0
benchmarkAvgTime = 0

for line in gcinfo_raw.readlines():
    if "nbThread" in line:
        last_nb_thread = int(line.split(" ")[-1][:-1])
        dico_gc[last_nb_thread] = 0
    elif "benchmarkAvgTime" in line:
        benchmarkAvgTime = int(line.split(" ")[-1])
        dico_gc[last_nb_thread] = (dico_gc[last_nb_thread]/benchmarkAvgTime)*100
    else:
        time_raw = line.split(" ")[-1]
        time_float = float(time_raw[:-3].replace(",","."))
        dico_gc[last_nb_thread] += time_float

print(dico_gc)
for nb_thread, time_gc in dico_gc.items():
    gcinfo.write(str(nb_thread) +" "+ str(time_gc/nb_test)+"\n")

gcinfo.close()
gcinfo_raw.close()
