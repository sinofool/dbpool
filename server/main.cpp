#include <Ice/Ice.h>
#include "dbpool_server.h"

using namespace net::sinofool::dbpool;

class DBPoolApp: virtual public Ice::Service {
public:
	virtual bool start(int argc, char* argv[], int& status) {
		_adapter = communicator()->createObjectAdapter("DBPool");
		DBPoolServerIPtr obj = new DBPoolServerI;
		_adapter->add(obj, communicator()->stringToIdentity("M"));
		_adapter->activate();
		status = EXIT_SUCCESS;
		return true;
	}
private:
	Ice::ObjectAdapterPtr _adapter;
};

int main(int argc, char* argv[]) {
	DBPoolApp app;
	return app.main(argc, argv);
}
