module net {
module sinofool {
module dbpool {
module idl {
	struct DBServer {
		string type;
		string host;
		string port;
		string db;
		string user;
		string pass;

		int coreSize;
		int maxSize;
		int idleTimeSeconds;
    
		string expression;
		int weight;
		string access;
	};
	sequence<DBServer> DBInstance;
	dictionary<string, DBInstance> DBInstanceDict;
	interface DBPoolServer {
		DBInstanceDict getDBInstanceDict();
		bool reload();
	};

};
};
};
};
