import os
from cassandra.auth import PlainTextAuthProvider
from cassandra.cluster import Cluster
from factories.abstractlist import AbstractList


class CassandraList(AbstractList):

    def __init__(self, id_list):
        self.id = id_list

        if os.getenv('CQLENG_ALLOW_SCHEMA_MANAGEMENT') is None:
            os.environ['CQLENG_ALLOW_SCHEMA_MANAGEMENT'] = '1'
        self.auth_provider = PlainTextAuthProvider(username='cassandra', password='cassandra')
        self.cluster = Cluster(protocol_version=3, auth_provider=self.auth_provider)
        self.session = self.cluster.connect()
        self.createkeyspace("cassandralist")
        self.session = self.cluster.connect("cassandralist")
        self.createcolumnfamily("clist")

    def append(self, elem):
        self.session.execute("""UPDATE cassandralist.clist SET ensemble = ensemble + [%s] WHERE id = %s;""", (elem, self.id))

    def remove(self, elem):
        self.session.execute("""UPDATE cassandralist.clist SET ensemble = ensemble - [%s] WHERE id = %s;""", (elem, self.id))

    def read(self):
        return list(self.session.execute("""SELECT ensemble FROM cassandralist.clist WHERE id = '%s';""" % self.id)[0][0])

    def createkeyspace(self, name):
        self.session.execute("""CREATE KEYSPACE IF NOT EXISTS %s WITH replication = { 'class' : 'SimpleStrategy', 
        'replication_factor' : 1 } AND durable_writes = false""" % name)

    def createcolumnfamily(self, name):
        self.session.execute("""CREATE TABLE IF NOT EXISTS %s (
                                id text,
                                ensemble list<text>,
                                PRIMARY KEY(id)
                            )""" % name)
