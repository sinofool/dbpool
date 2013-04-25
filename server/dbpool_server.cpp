#include "dbpool_server.h"
#include "log.h"
#include <sstream>

namespace net {
namespace sinofool {
namespace dbpool {

void DBConfigHandler::startElement(const std::string& name,
		const IceXML::Attributes& attr, int line, int column) {
	DBPOOLLOG_DEBUG(
			"(Line: " << line << " Column: " << column << ")\t" << "StartElement " << name);
	for (IceXML::Attributes::const_iterator it = attr.begin(); it != attr.end();
			++it) {
		DBPOOLLOG_DEBUG("\t" << it->first << "=" << it->second);
	}

	if (name == "pool") {
		DBPOOLLOG_DEBUG("START");
	} else if (name == "instance") {
		std::string name = findAttr(attr, "name");
		idl::DBInstance ins;
		std::pair<idl::DBInstanceDict::iterator, bool> ret = _data.insert(
				std::make_pair(name, ins));
		if (ret.second) {
			_current_instance = &(ret.first->second);
		} else {
			// duplicate instance name exists in file;
		  DBPOOLLOG_DEBUG("duplicate instance name = " << name << " exists in file");
			_current_instance = NULL;
		}
	} else if (name == "server") {
		idl::DBServer ser;
		ser.type = findAttr(attr, "type");
		ser.host = findAttr(attr, "host");
		ser.port = findAttr(attr, "port");
		ser.user = findAttr(attr, "user");
		ser.pass = findAttr(attr, "pass");
		ser.coreSize = findAttrAsInt(attr, "coreSize", 1);
		ser.maxSize = findAttrAsInt(attr, "maxSize", 10);
		ser.idleTimeSeconds = findAttrAsInt(attr, "idleTimeSeconds", 60);
		ser.expression = findAttr(attr, "expression");
		ser.weight = findAttrAsInt(attr, "weight", 100);
		ser.access = findAttr(attr, "access");
		_current_instance->push_back(ser);
		_current_server = _current_instance->data()
				+ (_current_instance->size() - 1);
	} else if (name == "host") {
		_current_value.str("");
		_current_value.clear();
	} else if (name == "port") {
		_current_value.str("");
		_current_value.clear();
	} else if (name == "user") {
		_current_value.str("");
		_current_value.clear();
	} else if (name == "pass") {
		_current_value.str("");
		_current_value.clear();
	} else if (name == "coreSize") {
		_current_value.str("");
		_current_value.clear();
	} else if (name == "maxSize") {
		_current_value.str("");
		_current_value.clear();
	} else if (name == "idleTimeSeconds") {
		_current_value.str("");
		_current_value.clear();
	} else if (name == "expression") {
		_current_value.str("");
		_current_value.clear();
	} else if (name == "weight") {
		_current_value.str("");
		_current_value.clear();
	} else if (name == "access") {
		_current_value.str("");
		_current_value.clear();
	}
}

void DBConfigHandler::endElement(const std::string& name, int line,
		int column) {
	DBPOOLLOG_DEBUG(
			"(Line: " << line << " Column: " << column << ")\t" << "EndElement " << name);
	if (name == "pool") {
		DBPOOLLOG_DEBUG("ALL.DONE.");
	} else if (name == "instance") {
		_current_instance = NULL;
	} else if (name == "server") {
		_current_server = NULL;
	} else if (name == "host") {
		_current_server->host = _current_value.str();
	} else if (name == "port") {
		_current_server->port = _current_value.str();
	} else if (name == "user") {
		_current_server->user = _current_value.str();
	} else if (name == "pass") {
		_current_server->pass = _current_value.str();
	} else if (name == "coreSize") {
		_current_server->coreSize = str2int(_current_value.str(), 1);
	} else if (name == "maxSize") {
		_current_server->maxSize = str2int(_current_value.str(), 10);
	} else if (name == "idleTimeSeconds") {
		_current_server->idleTimeSeconds = str2int(_current_value.str(), 60);
	} else if (name == "expression") {
		_current_server->expression = _current_value.str();
	} else if (name == "weight") {
		_current_server->weight = str2int(_current_value.str(), 100);
	} else if (name == "access") {
		_current_server->access = _current_value.str();
	}
}

void DBConfigHandler::characters(const std::string& name, int line,
		int column) {
	DBPOOLLOG_DEBUG(
			"(Line: " << line << " Column: " << column << ")\t" << "Characters " << name);
	_current_value << name;
}

std::string DBConfigHandler::findAttr(const IceXML::Attributes& attr,
		const std::string& key) {
	DBPOOLLOG_DEBUG("Finding " << key);
	IceXML::Attributes::const_iterator it = attr.find(key);
	if (it == attr.end()) {
		return "";
	}
	return it->second;
}

int DBConfigHandler::findAttrAsInt(const IceXML::Attributes& attr,
		const std::string& key, int def) {
	std::string str = findAttr(attr, key);
	if (str == "") {
		return def;
	}
	return str2int(str, def);
}

int DBConfigHandler::str2int(const std::string& str, int def) {
	std::istringstream v(str.c_str());
	int val = def;
	if (!(v >> def) || !v.eof()) {
		return val;
	}
	return def;
}

DBPoolServerI::DBPoolServerI() {
	_reload();
}

idl::DBInstanceDict DBPoolServerI::getDBInstanceDict(const Ice::Current&) {
  // when _data is serialized, it may cause core, so we copy _data and return the copy
  idl::DBInstanceDict data;
  {
    IceUtil::Mutex::Lock lock(_mutex_data);
    data = _data;
  }
	return data;
}

bool DBPoolServerI::reload(const Ice::Current&) {
	return _reload();
}

bool DBPoolServerI::_reload() {
	DBConfigHandler handle;
  
  try {
      IceXML::Parser::parse("dbpool.xml", handle);
  } catch (IceXML::ParserException& e) {
			DBPOOLLOG_DEBUG("reload xml got a parse exception " << e.what());
      return false;
  }

  idl::DBInstanceDict data = handle.getDBInstanceDict();
	{
		IceUtil::Mutex::Lock lock(_mutex_data);
		_data = data;
	}

	std::set<idl::DBPoolClientPrx, DBPoolClientPrxLessTo> clients;
	{
		IceUtil::Mutex::Lock lock(_mutex_clients);
		clients = _clients;
	}
	for (std::set<idl::DBPoolClientPrx>::iterator it = clients.begin();
			it != clients.end(); ++it) {
		try {
			(*it)->pushDBInstanceDict(data);
			DBPOOLLOG_DEBUG("Pushed to " << (*it));
		} catch (...) {
			IceUtil::Mutex::Lock lock(_mutex_clients);
			_clients.erase(*it);
			DBPOOLLOG_DEBUG(
					"Error push to " << (*it) << ". Current have " << _clients.size() << " clients.");
		}
	}
	return true;
}

bool DBPoolServerI::registerClient(const idl::DBPoolClientPrx& client,
		const Ice::Current&) {
	IceUtil::Mutex::Lock lock(_mutex_clients);
  // 1. add timeout to make sure when someone use the clientprx in a wrong way
  // they can't hang our service
  // 2. same client form the same prx? if not the set will have more than one prx of one client
  // we can use prx->ice_getIdentity() to compare whether the two proxies is stands as the same client
	_clients.insert(client->ice_timeout(300));
	DBPOOLLOG_DEBUG(
			"Client registered " << client << " current have " << _clients.size() << " clients.");
	return true;
}
}
}
}
