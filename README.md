# DBPool
## Introduction

This is a system for easy management Database Connection for Java development

## USAGE
### Maven dependency
Use following dependency in your project:
<pre>
&lt;dependency&gt;
    &lt;groupId&gt;net.sinofool&lt;/groupId&gt;
    &lt;artifactId&gt;dbpool&lt;/artifactId&gt;
    &lt;version&gt;1.1&lt;/version&gt;
&lt;/dependency&gt;
</pre>
## INSTALL

### Java Client
It needs enviroument variable ICE_HOME set to where ICE is installed.

	export ICE_HOME=$HOME/build/Ice-3.5
	cd client-java && mvn package

### C++ Server
It needs two variables: ICE_HOME LOG4CPLUS_HOME

	cd server && make ICE_HOME=$HOME/build/Ice-3.5 LOG4CPLUS_HOME=$HOME/build/log4cplus_dist

## Precompiled package
I have some precompiled packages for quick start in pure java environment.
###For CentOS6:
dbpool_server:	http://sinofool.net/dl/dbpool_server.tar.bz2
Ice-3.5.0:	http://sinofool.net/dl/Ice-3.5.0-binary-centos6.tar.bz2

If you want package for other platform, please open a ticket in this project.
