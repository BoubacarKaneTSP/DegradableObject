import sys
import os

benchmark_type = sys.argv[1]
type_obj = sys.argv[2]
if benchmark_type == 'Retwis':
    nb_user = sys.argv[3]
    completion_time = sys.argv[4]

if benchmark_type == "Retwis":
    if completion_time ==  "True":
        list_op = ["ALL"]
    else:
        list_op = ["ALL", "ADD", "FOLLOW", "UNFOLLOW", "TWEET", "READ", "COUNT","GROUPE","PROFILE","avg_time_computed"]#, "avg_queue_size", "avg_Follower", "avg_Following", "proportion_Max_Follower", "proportion_Max_Following","proportion_User_With_Max_Following", "proportion_User_With_Max_Follower", "proportion_User_Without_Follower","proportion_User_Without_Following", "nb_user_final", "nb_tweet_final"]
else:
    list_op = ["ALL", "ADD", "REMOVE", "READ"]

for op in list_op:
    try:
        if benchmark_type == "Retwis":
            os.remove(op+"_"+type_obj+"_"+nb_user+".txt")
        else:
            os.remove("microbenchmark_results/"+type_obj+"_"+op+".txt")
    except FileNotFoundError:
        pass
