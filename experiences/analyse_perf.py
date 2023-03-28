import sys

perf_file_name = sys.argv[1]
object_name = sys.argv[2]
nb_thread = sys.argv[3]
nb_user = sys.argv[4]

perf_log_raw = open(perf_file_name,"r")

list_event = ["cache-references", "cache-misses", "branch-misses", "branches", "cycles", "instructions"]

dico_stat_event = dict()
dico_file_event = dict()

append = ""

if nb_thread == "1":
    append = "w"
else:
    append = "a"

for event in list_event:
    dico_stat_event[event] = None
    dico_file_event[event] = open("perf_"+event+"_"+object_name+"_"+nb_user+".txt", append)

ratio_cache_misses = open("perf_ratio_cache_misses_"+object_name+"_"+nb_user+".txt", append)
ratio_branch_misses = open("perf_ratio_branch_misses_"+object_name+"_"+nb_user+".txt", append)
instruction_per_cycle = open("perf_instruction_per_cycle_"+object_name+"_"+nb_user+".txt", append)

for line in perf_log_raw.readlines():

    line = line.strip()
    
    # print(line)
    # print("-------------------")
    for event in list_event:
        if event in line:
            if dico_stat_event[event] == None :
                val = line.partition(event)[0].strip()
                dico_stat_event[event] = val



for k,v in dico_stat_event.items():

    dico_file_event[k].write(nb_thread + " " + v + "\n")
    dico_file_event[k].close()
    # print("key : " + k +",","value : " + v)

ratio_cache_misses.write(nb_thread + " " + str(int(dico_stat_event["cache-misses"]) / int(dico_stat_event["cache-references"]) * 100) + "\n")
ratio_branch_misses.write(nb_thread + " " + str(int(dico_stat_event["branch-misses"]) / int(dico_stat_event["branches"]) * 100) + "\n")
instruction_per_cycle.write(nb_thread + " " + str(int(dico_stat_event["instructions"]) / int(dico_stat_event["cycles"])) + "\n")

ratio_cache_misses.close()
ratio_branch_misses.close()
instruction_per_cycle.close()
perf_log_raw.close()