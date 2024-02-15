import redis
from factories.abstractlist import AbstractList


class RedisList(AbstractList):

    def __init__(self, id_list):
        self.id = id_list
        self.r = redis.StrictRedis(host='localhost', port=6379, db=0)

    def append(self, elem):
        self.r.lpush(self.id, elem)

    def remove(self, elem):
        self.r.lrem(self.id, 0, elem)

    def read(self):
        result = list()

        for elem in self.r.lrange(self.id, 0, -1):
            result.append(elem.decode('utf-8'))

        return result
