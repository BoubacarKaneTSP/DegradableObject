from factories.abstractlist import AbstractList
from cassandra.cluster import Cluster
from cassandra.cqlengine.management import sync_table
from cassandra.cqlengine.connection import register_connection
from cassandra.cqlengine.connection import set_default_connection
from cassandra.auth import PlainTextAuthProvider
from factories import CounterFactory
import os
from cassandra.cqlengine.query import LWTException


class DegradedCassandraList(AbstractList):

	all_list = {}
	last_read = list()
	
	def __init__(self, id_new_list): # Connect to cassandra

		self.id = id_new_list

		if not self.all_list: #Check if the dict is empty
			self.connect()
		
		try:
			self.all_list[self.id] = DList.if_not_exists().create(id_list=self.id, id_writer=str(os.getpid()))
			self.Count = CounterFactory().create_counter("CCF",self.id)
		except LWTException as e:
			print("already exists")
			self.all_list[self.id] = DList.objects.filter(id_list=self.id).get()
			self.Count = CCounter.objects.filter(id=self.id).get()

	def add(self, elem):
		
		pass

	def remove(self, elem):
		pass

	def read(self):
		
		new_read = list()
		result = list()
		values = list()
		
		for q in DList.objects(DList.id_list == self.id).allow_filtering().all():
			for tmp in q.ensemble:
				tmp_tuple = tmp.split(":")
				new_read.append(list(tmp_tuple))
		
		result = self.last_read + sorted([ele for ele in new_read if ele not in self.last_read])
		self.last_read = result
		
		values = [ele[1] for ele in result]
		return (values)
		
	def connect(self):
		if os.getenv('CQLENG_ALLOW_SCHEMA_MANAGEMENT') is None:
			os.environ['CQLENG_ALLOW_SCHEMA_MANAGEMENT'] = '1'
		self.auth_provider = PlainTextAuthProvider(username='cassandra', password='cassandra')
		self.cluster = Cluster(protocol_version=3,auth_provider=self.auth_provider)
		self.session = self.cluster.connect()
		#self.session.execute("DROP KEYSPACE IF EXISTS degradedlist")
		self.session.execute("CREATE KEYSPACE IF NOT EXISTS degradedlist WITH replication = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 } AND durable_writes = false")
		self.session = self.cluster.connect("degradedlist")
		register_connection(str(self.session), session=self.session)
		set_default_connection(str(self.session))

		sync_table(DList)
