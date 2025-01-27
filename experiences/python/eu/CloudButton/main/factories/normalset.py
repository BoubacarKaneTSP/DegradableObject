from factories.abstractset import AbstractSet

class NormalSet(AbstractSet):
	
	all_set = {}
	
	def __init__(self, id_proc):
		
		self.id = id_proc

		if self.id not in self.all_set:
			self.all_set[self.id] = set()
		
	def add(self, elem):
		self.all_set[self.id].add(elem)
		
	def remove(self, elem):
		self.all_set[self.id].remove(elem)
	def read(self):
		return self.all_set[self.id]
