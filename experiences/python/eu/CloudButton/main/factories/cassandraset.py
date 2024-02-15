import os
from cassandra.auth import PlainTextAuthProvider
from cassandra.cluster import Cluster
from factories.abstractset import AbstractSet


class CassandraSet(AbstractSet):

    def __init__(self, id_test):
        self.id = id_test

        if os.getenv('CQLENG_ALLOW_SCHEMA_MANAGEMENT') is None:
            os.environ['CQLENG_ALLOW_SCHEMA_MANAGEMENT'] = '1'
        self.auth_provider = PlainTextAuthProvider(username='cassandra', password='cassandra')
        self.cluster = Cluster(protocol_version=3, auth_provider=self.auth_provider)
        self.session = self.cluster.connect()
        self.createkeyspace("cassandraset")
        self.session = self.cluster.connect("cassandraset")
        self.createcolumnfamily("cset")

    def add(self, elem):
        self.session.execute("""UPDATE cassandraset.cset SET ensemble = ensemble + {%s} WHERE id = %s;""", (elem, self.id))

    def remove(self, elem):
        self.session.execute("""UPDATE cassandraset.cset SET ensemble = ensemble - {%s} WHERE id = %s;""", (elem, self.id))

    def read(self):
        return set(self.session.execute("""SELECT ensemble FROM cassandraset.cset WHERE id = '%s';""" % self.id)[0][0])

    def createkeyspace(self, name):
        self.session.execute("""CREATE KEYSPACE IF NOT EXISTS %s WITH replication = { 'class' : 'SimpleStrategy', 
        'replication_factor' : 1 } AND durable_writes = false""" % name)

    def createcolumnfamily(self, name):
        self.session.execute("""CREATE TABLE IF NOT EXISTS %s (
                                id text,
                                ensemble set<text>,
                                PRIMARY KEY(id)
                            )""" % name)
