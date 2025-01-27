from unittest import TestCase
from factories import NormalList

class TestNormalList(TestCase):
    def setUp(self):
        self.list = NormalList("test")

    def tearDown(self):
        self.list.all_list["test"] = list()

    def test_read(self):
        self.list.append("v1")
        self.list.append("v2")
        self.assertEqual(list(["v1", "v2"]), self.list.read(), msg="Normal list read failed")

    def test_append(self):
        self.list.append("v1")
        self.assertEqual("v1", self.list.all_list["test"].pop(0), msg="Normal list add failed")

    def test_remove(self):
        self.list.all_list["test"].append("v1")
        self.list.remove("v1")
        self.assertEqual(list(), self.list.all_list["test"], msg="Normal list remove failed")

