from factories.abstractset import AbstractSet
from cassandra.cluster import Cluster
from cassandra.auth import PlainTextAuthProvider
import os


class DegradedCassandraSet(AbstractSet):

	def __init__(self, id_test):
		self.id = id_test
		self.os_pid = str(os.getpid())

		if os.getenv('CQLENG_ALLOW_SCHEMA_MANAGEMENT') is None:
			os.environ['CQLENG_ALLOW_SCHEMA_MANAGEMENT'] = '1'
		self.auth_provider = PlainTextAuthProvider(username='cassandra', password='cassandra')
		self.cluster = Cluster(protocol_version=3, auth_provider=self.auth_provider)
		self.session = self.cluster.connect()
		self.createkeyspace("degradedset")
		self.session = self.cluster.connect("degradedset")
		self.createcolumnfamily("dset")

	def add(self, elem):
		self.session.execute("""UPDATE degradedset.dset SET ensemble = ensemble + {%s} WHERE id_set = %s AND id_writer = %s;""", (elem, self.id, self.os_pid))

	def remove(self, elem):
		self.session.execute("""UPDATE degradedset.dset SET ensemble = ensemble - {%s} WHERE id_set = %s AND id_writer = %s;""", (elem, self.id, self.os_pid))

	def read(self):
		total = set()

		for ensemble in self.session.execute("""SELECT ensemble FROM degradedset.dset WHERE id_set = '%s';""" % self.id).all():
			total = total.union(set(ensemble[0]))

		return sorted(total)

	def createkeyspace(self, name):
		self.session.execute("""CREATE KEYSPACE IF NOT EXISTS %s WITH replication = { 'class' : 'SimpleStrategy', 
			'replication_factor' : 1 } AND durable_writes = false""" % name)

	def createcolumnfamily(self, name):
		self.session.execute("""CREATE TABLE IF NOT EXISTS %s (
									id_set text,
									id_writer text,
									ensemble set<text>,
									PRIMARY KEY(id_set, id_writer)
								)""" % name)

