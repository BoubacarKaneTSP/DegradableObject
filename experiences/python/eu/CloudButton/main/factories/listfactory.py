class ListFactory(object):
	
	@classmethod
	def create_list(self, targetclass, id_list):

		if targetclass == "DCList":
			return eval("DegradedCassandraList(id_list)")
			
		if targetclass == "NList":
			return eval("NormalList(id_list)")
			
		if targetclass == "CList":
			return eval("CassandraList(id_list)")

		if targetclass == "RList":
			return eval("RedisList(id_list)")
