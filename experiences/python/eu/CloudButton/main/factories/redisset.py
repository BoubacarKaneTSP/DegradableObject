import redis
from factories.abstractset import AbstractSet


class RedisSet(AbstractSet):

    def __init__(self, id_test):
        self.id = id_test
        self.r = redis.StrictRedis(host='localhost', port=6379, db=0)

    def add(self, elem):
        self.r.sadd(self.id, elem)

    def remove(self, elem):
        self.r.srem(self.id, elem)

    def read(self):
        result = set()

        for elem in self.r.smembers(self.id):
            result.add(elem.decode('utf-8'))

        return sorted(result)
