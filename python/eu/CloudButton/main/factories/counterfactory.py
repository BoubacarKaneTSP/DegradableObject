from python.eu.CloudButton.main.factories import rediscounter

class CounterFactory(object):
	
	@classmethod
	def create_counter(self, targetclass, id_count):
		
		if targetclass == "CCounter":
			return eval("CassandraCounter(id_count)")
			
		if targetclass == "MRCCounter":
			return eval("MultipleRowCounter(id_count)")

		if targetclass == "RCounter":
			return eval("RedisCounter(id_count)")
		
		else:
			raise TypeError("This type of counter do not exists")
