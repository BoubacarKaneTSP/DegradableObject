from unittest import TestCase

from factories import CassandraCounter


class TestCassandraCounter(TestCase):
    def setUp(self):
        self.counter = CassandraCounter("test")

    def test_increment(self):
        self.counter.increment(1)
        self.assertEqual(1, self.counter.all_counter["test"].count, msg="Cassandra counter increment failed")

    def test_read(self):
        self.counter.all_counter["test"].count+=1
        self.assertEqual(1, self.counter.read(), msg="Cassandra counter read failed")