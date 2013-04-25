#include "dbpool.h"
#include <functional>
#include <IceXML/Parser.h>
#include <IceUtil/RecMutex.h>

namespace net {
namespace sinofool {
namespace dbpool {


class DBConfigHandler: virtual public IceXML::Handler {
public:
	virtual ~DBConfigHandler() {
	}

	virtual void startElement(const std::string&, const IceXML::Attributes&,
			int, int);
	virtual void endElement(const std::string&, int, int);
	virtual void characters(const std::string&, int, int);

	idl::DBInstanceDict getDBInstanceDict() {
		return _data;
	}
private:
	std::string findAttr(const IceXML::Attributes& attr,
			const std::string& key);
	int findAttrAsInt(const IceXML::Attributes& attr, const std::string& key,
			int def);
	inline int str2int(const std::string& str, int def);

	idl::DBInstanceDict _data;
	idl::DBInstance* _current_instance;
	idl::DBServer* _current_server;
	std::ostringstream _current_value;
};
typedef IceUtil::Handle<DBConfigHandler> DBPoolHandlerPtr;

struct DBPoolClientPrxLessTo: public std::binary_function<idl::DBPoolClientPrx, idl::DBPoolClientPrx, bool> 
{
  bool operator()(const idl::DBPoolClientPrx& __x, const idl::DBPoolClientPrx& __y) const {
    return __x->ice_getIdentity() < __y->ice_getIdentity();
  }
};

class DBPoolServerI: virtual public idl::DBPoolServer {
public:
	DBPoolServerI();
	virtual idl::DBInstanceDict getDBInstanceDict(const Ice::Current&);
	virtual bool reload(const Ice::Current&);
	virtual bool registerClient(const idl::DBPoolClientPrx&, const Ice::Current&);
private:
	bool _reload();
	idl::DBInstanceDict _data;
	IceUtil::Mutex _mutex_data;
	IceUtil::Mutex _mutex_clients;
	std::set<idl::DBPoolClientPrx,DBPoolClientPrxLessTo> _clients;
};
typedef IceUtil::Handle<DBPoolServerI> DBPoolServerIPtr;
}
}
}
