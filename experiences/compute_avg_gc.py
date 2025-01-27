import sys

def calculate_bounds(values_func):
    # Calcul de la moyenne, du max et du min
    mean_func = sum(values_func) / len(values_func)
    max_value_func = max(values_func)
    min_value_func = min(values_func)

    # Création des listes qui contiennent les valeurs supérieur ou inférieur à la moyenne
    values_sup_mean = [value for value in values_func if value >= mean_func]
    values_inf_mean = [value for value in values_func if value < mean_func]

    # Calcul de la borne supérieure et inférieure
    if len(values_sup_mean) > 0:
        upper_bound_func = sum(values_sup_mean) / len(values_sup_mean)
    else:
        upper_bound_func = mean_func

    if len(values_inf_mean) > 0:
        lower_bound_func = sum(values_inf_mean) / len(values_inf_mean)
    else:
        lower_bound_func = mean_func

    return mean_func, upper_bound_func, lower_bound_func, max_value_func, min_value_func

nb_user = sys.argv[1]
type_obj = sys.argv[2]
list_nb_thread = sys.argv[3]
completion_time = sys.argv[4]

list_nb_thread = list_nb_thread.split(" ")

tag_spe = "_ExtConcHashMapNoCleanTLPut"
name_file=type_obj+"_"+nb_user+"_gc_usage"+tag_spe+".txt"

file = open(name_file,"r")

str_result_avg = ""

for nb_thread in list_nb_thread:

    val = 0

    if nb_thread == list_nb_thread[0]:
        line = file.readline()
        line = line.strip().split(" ")

    values = []

    while line[0] == nb_thread:
        val = float(line[1])
        values.append(val)
        line = file.readline()
        line = line.strip().split(" ")

    print(name_file)
    mean, upper_bound, lower_bound, max_value, min_value = calculate_bounds(values)

    str_result_avg += nb_thread + " " + str(mean) + " " + str(upper_bound)+ " " + str(lower_bound) + " " + str(max_value) + " " + str(min_value) + "\n"

file.close()

file = open(type_obj+"_"+nb_user+"_gc_usage"+tag_spe+".txt","w")

file.write(str_result_avg)
file.close()
