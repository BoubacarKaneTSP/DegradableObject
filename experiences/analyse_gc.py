import sys

object_name = sys.argv[1]
nb_test = int(sys.argv[2])
nb_user = sys.argv[4]

tag_spe = "_ExtHashNoCleanTL"
# tag_spe = "_ConcHashNoCleanTL"

gcinfo_raw = open(object_name+"_gcinfo.log","r")
gcinfo = open(object_name + "_" + nb_user + "_gc_usage"+tag_spe+".txt","a")
dico_gc = dict()

last_nb_thread = 0
benchmarkAvgTime = 0
flag_benchmark = False


for line in gcinfo_raw.readlines():
    if "Start benchmark" in line:
        flag_benchmark = True
        continue
    elif "End benchmark" in line:
        flag_benchmark = False
        continue

    if "nbThread" in line:
        last_nb_thread = int(line.split(" ")[-1][:-1])
        dico_gc[last_nb_thread] = 0
        continue
    elif "benchmarkAvgTime" in line:
        benchmarkAvgTime = int(line.split(" ")[-1])
        # dico_gc[last_nb_thread] = (dico_gc[last_nb_thread]/benchmarkAvgTime)*100
        continue
        
    if flag_benchmark:
        time_raw = line.split(" ")[-1]
        time_float = float(time_raw[:-3].replace(",","."))
        dico_gc[last_nb_thread] += time_float

for nb_thread, time_gc in dico_gc.items():
    gcinfo.write(str(nb_thread) +" "+ str(time_gc/nb_test)+"\n")


gcinfo.close()
gcinfo_raw.close()
