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
    max_val = np.max(a)
    min_val = np.min(a)

    return m, m + h, m - h, max_val, min_val

nb_user = sys.argv[1]
type_obj = sys.argv[2]
completion_time = sys.argv[3]
list_nb_thread = sys.argv[4:]

if completion_time == "True":
    list_op = ["ALL"]
else:
    list_op = ["ALL",
               "ADD",
               "FOLLOW",
               "UNFOLLOW",
               "TWEET",
               "READ",
               "COUNT",
               "GROUPE",
               "PROFILE",
               "avg_time_computed"]#,
               #"avg_queue_size",
               #"avg_Follower",
               #"avg_Following",
               #"proportion_Max_Follower",
               #"proportion_Max_Following",
               #"proportion_User_With_Max_Follower",
               #"proportion_User_With_Max_Following",
               #"proportion_User_Without_Follower",
               #"proportion_User_Without_Following",
               #"nb_user_final",
           #"nb_tweet_final"]

for op in list_op:
    name_file=op+"_"+type_obj+"_"+nb_user+".txt"
    try:
        file = open(name_file,"r")
    except FileNotFoundError:
        print(name_file+" not found, cannot compute average performances")
        sys.exit(1)

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

    file = open(op+"_"+type_obj+"_"+nb_user+".txt","w")

    file.write(str_result_avg)
    file.close()
