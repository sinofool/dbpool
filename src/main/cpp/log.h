#include <log4cplus/logger.h>
#include <log4cplus/loggingmacros.h>

#define DBPOOLLOG_DEBUG(msg) 																	\
	do { 																						\
		log4cplus::Logger logger = log4cplus::Logger::getInstance(LOG4CPLUS_TEXT("dbpoollog"));	\
		LOG4CPLUS_DEBUG(logger, msg);															\
	}while(false);
