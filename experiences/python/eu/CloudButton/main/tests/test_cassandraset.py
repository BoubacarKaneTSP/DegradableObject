from unittest import TestCase
from factories.cassandraset import CassandraSet
from cassandra.cluster import Cluster
from cassandra.auth import PlainTextAuthProvider


class TestCassandraSet(TestCase):

    def setUp(self):
        self.set = CassandraSet("test")

    def tearDown(self):
        auth = PlainTextAuthProvider(username='cassandra', password='cassandra')
        cluster = Cluster(protocol_version=3, auth_provider=auth)
        session = cluster.connect()
        #session.execute("""DROP KEYSPACE IF EXISTS cassandraset""")

    def test_add(self):
        self.set.add("v1")
        self.set.add("v2")
        self.assertEqual(set(["v1", "v2"]), self.set.all_set["test"].ensemble, msg="Cassandra set add failed")

    def test_read(self):
        self.assertEqual(set(["v1", "v2"]), self.set.read(), msg="Cassandra set read failed")

    def test_remove(self):
        self.set.remove("v1")
        self.assertEqual(set(["v2"]), self.set.all_set["test"].ensemble, msg="Cassandra set add failed")
