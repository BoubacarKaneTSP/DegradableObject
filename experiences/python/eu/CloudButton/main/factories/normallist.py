from factories.abstractlist import AbstractList

class NormalList(AbstractList):
	
	all_list = {}

	def __init__(self, id_list):
		
		self.id = id_list

		if self.id not in self.all_list:
			self.all_list[self.id] = list()
		
	def append(self, elem):
		self.all_list[self.id].append(elem)
		
	def remove(self, elem):
		self.all_list[self.id].remove(elem)
	def read(self):
		return self.all_list[self.id]
