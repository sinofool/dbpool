#include "dbpool_server.h"
#include <sstream>

namespace net {
namespace sinofool {
namespace dbpool {

void DBConfigHandler::startElement(const std::string& name,
		const IceXML::Attributes& attr, int line, int column) {
	std::cout << "(Line: " << line << " Column: " << column << ")\t"
			<< "StartElement " << name;
	for (IceXML::Attributes::const_iterator it = attr.begin(); it != attr.end();
			++it) {
		std::cout << "\t" << it->first << "=" << it->second;
	}
	std::cout << std::endl;

	if (name == "pool") {
		std::cout << "START" << std::endl;
	} else if (name == "instance") {
		std::string name = findAttr(attr, "name");
		idl::DBInstance ins;
		std::pair<idl::DBInstanceDict::iterator, bool> ret = _data.insert(
				std::make_pair(name, ins));
		if (ret.second) {
			_current_instance = &(ret.first->second);
		} else {
			// duplicate instance name exists in file;
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
		std::cout << "Start Step 1 " << _current_instance << std::endl;
		_current_instance->push_back(ser);
		std::cout << "Start Step 2" << std::endl;
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
	std::cout << "StartEnd" << std::endl;
}

void DBConfigHandler::endElement(const std::string& name, int line, int column) {
	if (name == "pool") {
		std::cout << "ALL.DONE." << std::endl;
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
	std::cout << "(Line: " << line << " Column: " << column << ")\t"
			<< "EndElement " << name << std::endl;
}

void DBConfigHandler::characters(const std::string& name, int line, int column) {
	_current_value << name;
	std::cout << "(Line: " << line << " Column: " << column << ")\t"
			<< "Characters " << name << std::endl;
}

std::string DBConfigHandler::findAttr(const IceXML::Attributes& attr,
		const std::string& key) {
	std::cout << "Finding " << key << std::endl;
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
	return _data;
}

bool DBPoolServerI::reload(const Ice::Current&) {
	return _reload();
}

bool DBPoolServerI::_reload() {
	DBConfigHandler handle;
	IceXML::Parser::parse("dbpool.xml", handle);
	_data = handle.getDBInstanceDict();
	return true;
}

}
}
}
