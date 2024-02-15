class SetFactory(object):

    @classmethod
    def create_set(self, targetclass, id_set):
        if targetclass == "NSet":
            return eval("NormalSet(id_set)")

        if targetclass == "DCSet":
            return eval("DegradedCassandraSet(id_set)")

        if targetclass == "CSet":
            return eval("CassandraSet(id_set)")

        if targetclass == "RSet":
            return eval("RedisSet(id_set)")
