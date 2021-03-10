from factories.abstractcounter import AbstractCounter
from cassandra.cluster import Cluster
from cassandra.auth import PlainTextAuthProvider
import os


class CassandraCounter(AbstractCounter):

    def __init__(self, id_counter):
        self.id = id_counter

        if os.getenv('CQLENG_ALLOW_SCHEMA_MANAGEMENT') is None:
            os.environ['CQLENG_ALLOW_SCHEMA_MANAGEMENT'] = '1'
        self.auth_provider = PlainTextAuthProvider(username='cassandra', password='cassandra')
        self.cluster = Cluster(protocol_version=3, auth_provider=self.auth_provider)
        self.session = self.cluster.connect()
        self.createkeyspace("cassandracounter")
        self.session = self.cluster.connect("cassandracounter")
        self.createcolumnfamily("ccounter")

    def increment(self, value):
        self.session.execute("""UPDATE cassandracounter.ccounter SET count = count + %s WHERE id = %s;""",
                             (value, self.id))

    def read(self):
        return self.session.execute("""SELECT count FROM cassandracounter.ccounter WHERE id = '%s';""" % self.id).one()[0]

    def createkeyspace(self, name):
        self.session.execute("""CREATE KEYSPACE IF NOT EXISTS %s WITH replication = { 'class' : 'SimpleStrategy', 
        'replication_factor' : 1 } AND durable_writes = false""" % name)

    def createcolumnfamily(self, name):
        self.session.execute("""CREATE TABLE IF NOT EXISTS %s (
                                id text,
                                count counter,
                                PRIMARY KEY(id)
                            )""" % name)
