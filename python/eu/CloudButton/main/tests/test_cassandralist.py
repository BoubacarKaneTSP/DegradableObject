from unittest import TestCase
from factories import CassandraList
from cassandra.cluster import Cluster
from cassandra.auth import PlainTextAuthProvider


class TestCassandraList(TestCase):
    def setUp(self):
        self.list = CassandraList("test")

    def tearDown(self):
        auth = PlainTextAuthProvider(username='cassandra', password='cassandra')
        cluster = Cluster(protocol_version=3, auth_provider=auth)
        session = cluster.connect()
        session.execute("""DROP KEYSPACE IF EXISTS cassandralist""")

    def test_append(self):
        self.list.append("v1")
        self.list.append("v2")
        self.assertEqual(list(["v1", "v2"]), self.list.all_list["test"].ensemble, msg="Cassandra list append failed")

    def test_read(self):

        self.list.append("v1")
        self.list.append("v2")
        self.assertEqual(list(["v1", "v2"]), self.list.read(), msg="Cassandra list append failed")