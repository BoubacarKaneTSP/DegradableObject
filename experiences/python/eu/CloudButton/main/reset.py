import redis

r = redis.StrictRedis(host='localhost', port=6379, db=0)
r.flushall()

if 1 == 0:
    if os.getenv('CQLENG_ALLOW_SCHEMA_MANAGEMENT') is None:
        os.environ['CQLENG_ALLOW_SCHEMA_MANAGEMENT'] = '1'

    auth = PlainTextAuthProvider(username='cassandra', password='cassandra')
    cluster = Cluster(protocol_version=3,auth_provider=auth)
    session = cluster.connect()

    session.execute("""DROP KEYSPACE IF EXISTS multiplerowcounter""")
    session.execute("""DROP KEYSPACE IF EXISTS degradedlist""")
    session.execute("""DROP KEYSPACE IF EXISTS degradedset""")
    session.execute("""DROP KEYSPACE IF EXISTS cassandracounter""")
    session.execute("""DROP KEYSPACE IF EXISTS cassandralist""")
    session.execute("""DROP KEYSPACE IF EXISTS cassandraset""")

    obj = CounterFactory().create_counter("CCounter", "test")

    obj = SetFactory().create_set("CSet","test")

    obj = ListFactory().create_list("CList","test")
