import sys

nb_user = sys.argv[1]
type_obj = sys.argv[2]
list_nb_thread = sys.argv[3]

list_nb_thread = list_nb_thread.split(" ")


list_op = ["ALL", "ADD", "FOLLOW", "UNFOLLOW", "TWEET", "READ", "avg_queue_size", "avg_Follower", "proportion_Max_Follower", "proportion_User_With_Max_Follower", "proportion_User_Without_Follower", "nb_user_final", "nb_tweet_final"]

for op in list_op:
    file = open(op+"_"+type_obj+"_139_"+nb_user+".txt","r")

    str_result_avg = ""

    for nb_thread in list_nb_thread:

        val = 0
        nb_test = 0

        if nb_thread == list_nb_thread[0]:
            line = file.readline()
            line = line.strip().split(" ")

        while line[0] == nb_thread:
            val += float(line[1])
            line = file.readline()
            line = line.strip().split(" ")
            nb_test += 1

        val = val/(int(nb_test))

        str_result_avg += nb_thread + " " + str(val) + "\n"

    file.close()

    file = open(op+"_"+type_obj+"_139_"+nb_user+".txt","w")

    file.write(str_result_avg)
    file.close()
