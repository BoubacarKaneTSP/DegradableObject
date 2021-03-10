from factories.abstractcounter import AbstractCounter
from cassandra.cluster import Cluster
from cassandra.auth import PlainTextAuthProvider
import os


class MultipleRowCounter(AbstractCounter):
	
	def __init__(self, id_counter):
		
		self.id = id_counter
		self.os_pid = str(os.getpid())

		if os.getenv('CQLENG_ALLOW_SCHEMA_MANAGEMENT') is None:
			os.environ['CQLENG_ALLOW_SCHEMA_MANAGEMENT'] = '1'
		self.auth_provider = PlainTextAuthProvider(username='cassandra', password='cassandra')
		self.cluster = Cluster(protocol_version=3, auth_provider=self.auth_provider)
		self.session = self.cluster.connect()
		self.createkeyspace("multiplerowcounter")
		self.session = self.cluster.connect("multiplerowcounter")
		self.createcolumnfamily("mrccounter")

	def increment(self, value):
		self.session.execute("""UPDATE multiplerowcounter.mrccounter SET count = count + %s WHERE id_counter = %s AND id_writer = %s;""",
							 (value, self.id, self.os_pid))

	def read(self):

		total = 0

		for val in self.session.execute("""SELECT count FROM multiplerowcounter.mrccounter WHERE id_counter = '%s';""" % self.id).all():
			total += val[0]

		return total

	def createkeyspace(self, name):
		self.session.execute("""CREATE KEYSPACE IF NOT EXISTS %s WITH replication = { 'class' : 'SimpleStrategy', 
			'replication_factor' : 1 } AND durable_writes = false""" % name)

	def createcolumnfamily(self, name):
		self.session.execute("""CREATE TABLE IF NOT EXISTS %s (
									id_counter text,
									id_writer text,
									count counter,
									PRIMARY KEY(id_counter, id_writer)
								)""" % name)