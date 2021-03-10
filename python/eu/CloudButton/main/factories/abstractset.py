from abc import abstractmethod, ABCMeta

class AbstractSet(metaclass=ABCMeta):		
	
	def __init__(self):
		self.all_set = {}
		
	@abstractmethod	
	def add(self, elem):
		pass
		
	@abstractmethod
	def remove(self, elem):
		pass
	
	@abstractmethod
	def read(self):
		pass
