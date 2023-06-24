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
    upper_bound_func = sum(values_sup_mean) / len(values_sup_mean)
    lower_bound_func = sum(values_inf_mean) / len(values_inf_mean)

    return mean_func, upper_bound_func, lower_bound_func, max_value_func, min_value_func

nb_user = sys.argv[1]
type_obj = sys.argv[2]
list_nb_thread = sys.argv[3]

list_nb_thread = list_nb_thread.split(" ")

list_op = ["ALL",
            "ADD",
           "FOLLOW",
           "UNFOLLOW",
           "TWEET",
           "READ",
           "avg_queue_size",
           "avg_Follower",
           "avg_Following",
           "proportion_Max_Follower",
           "proportion_Max_Following",
           "proportion_User_With_Max_Follower",
           "proportion_User_With_Max_Following",
           "proportion_User_Without_Follower",
           "proportion_User_Without_Following",
           "nb_user_final",
           "nb_tweet_final"]

for op in list_op:
    file = open(op+"_"+type_obj+"_"+nb_user+".txt","r")

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

        mean, upper_bound, lower_bound, max_value, min_value = calculate_bounds(values)

        str_result_avg += nb_thread + " " + str(mean) + " " + str(upper_bound)+ " " + str(lower_bound) + " " + str(max_value) + " " + str(min_value) + "\n"

    file.close()

    file = open(op+"_"+type_obj+"_"+nb_user+".txt","w")

    file.write(str_result_avg)
    file.close()
