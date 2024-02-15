from abc import abstractmethod, ABCMeta

class AbstractCounter(metaclass=ABCMeta):
	
	def __init__(self):
		pass
		
	@abstractmethod
	def increment(self, val):
		pass
	
	@abstractmethod
	def read(self):
		pass
