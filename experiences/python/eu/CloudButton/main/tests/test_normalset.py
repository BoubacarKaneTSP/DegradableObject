from unittest import TestCase

from factories import NormalSet


class TestNormalSet(TestCase):
    def setUp(self):
        self.set = NormalSet("test")

    def tearDown(self):
        self.set.all_set["test"] = set()

    def test_read(self):
        self.set.add("v1")
        self.set.add("v2")
        self.assertEqual(set(["v1", "v2"]), self.set.read(), msg="Normal set read failed")

    def test_add(self):
        self.set.add("v1")
        self.assertEqual("v1", self.set.all_set["test"].pop(), msg="Normal set add failed")

    def test_remove(self):
        self.set.all_set["test"].add("v1")
        self.set.remove("v1")
        self.assertEqual(set(), self.set.all_set["test"], msg="Normal set remove failed")

