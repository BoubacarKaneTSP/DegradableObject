import sys
import numpy as np
import scipy.stats as st

def calculate_bounds(values_func):
    # Calcul de la moyenne, du max et du min
    confidence = 0.95

    a = 1.0 * np.array(values_func)
    n = len(a)
    m, se = np.mean(a), st.sem(a)
    h = se * st.t.ppf((1 + confidence) / 2., n-1)
    try:
        max_val = np.max(a)
    except ValueError:
        print(a)
        exit(0)
    min_val = np.min(a)

    return m, m + h, m - h, max_val, min_val

print(sys.argv)
type_obj = sys.argv[1]
list_nb_thread = sys.argv[2]

list_nb_thread = list_nb_thread.split(" ")

list_op = ["ALL",
           "ADD",
           "REMOVE",
           "READ"]

for op in list_op:
    file = open("microbenchmark_results/"+type_obj + "_" + op + ".txt","r")

    str_result_avg = ""

    for nb_thread in list_nb_thread:

        val = 0

        if nb_thread == list_nb_thread[0]:
            line = file.readline()
            line = line.strip().split(" ")

        values = []

        while line[0] == nb_thread:
            if line[1] == "NaN":
                val = 0
            else:
                val = float(line[1])

            values.append(val)
            line = file.readline()
            line = line.strip().split(" ")

        mean, upper_bound, lower_bound, max_value, min_value = calculate_bounds(values)

        str_result_avg += nb_thread + " " + str(mean) + " " + str(upper_bound)+ " " + str(lower_bound) + " " + str(max_value) + " " + str(min_value) + "\n"

    file.close()

    file = open(type_obj + "_" + op + ".txt","w")

    file.write(str_result_avg)
    file.close()
