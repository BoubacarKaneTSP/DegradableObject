import sys
import os

nb_user = sys.argv[1]
type_obj = sys.argv[2]

list_op = ["ALL", "ADD", "FOLLOW", "UNFOLLOW", "TWEET", "READ", "avg_queue_size", "avg_Follower", "proportion_Max_Follower", "proportion_User_With_Max_Follower", "proportion_User_Without_Follower", "nb_user_final", "nb_tweet_final"]

for op in list_op:
    os.remove(op+"_"+type_obj+"_"+nb_user+".txt")
