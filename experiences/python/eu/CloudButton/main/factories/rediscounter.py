from python.eu.CloudButton.main.factories import abstractcounter
import redis


class RedisCounter(abstractcounter):

    def __init__(self, id_counter):
        self.id = id_counter
        self.r = redis.StrictRedis(host='localhost', port=6379, db=0)

    def increment(self, value):
        self.r.incrby(self.id, value)

    def read(self):
        return self.r.get(self.id).decode('utf-8')
