# DBPool
## Introduction

This is a system for easy management Database Connection for Java development

## INSTALL

### Java Client
It needs enviroument variable ICE_HOME set to where ICE is installed.

	export ICE_HOME=$HOME/build/Ice-3.5
	cd client-java && mvn package

### C++ Server
It needs two variables: ICE_HOME LOG4CPLUS_HOME

	cd server && make ICE_HOME=$HOME/build/Ice-3.5 LOG4CPLUS_HOME=$HOME/build/log4cplus_dist

