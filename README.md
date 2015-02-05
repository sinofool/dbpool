# DBPool
## Introduction
This is a system for easy management database connections.

There are two types of database managment model in general: proxy vs configuration
DBPool is an implemention of configuration managment.

There is no difference of using proxy or configuration from the users' aspective.
We concern about very large scale of deployment.
It is a balance between scalability and consistency, we choose the former one.
The system using this project should always consider the database connection is never switched at the same time.
Redesign your system if it requires strong consistency in a distributed environment, which is too much costly.

## History &amp; Roadmap
This project comes from the idea when I was working at Renren Inc. (NYSE:RENN) 2006-2012.
There are lots of friends asked me about how the system worked after they left Renren company.
I decided to write it again from scratch and open source.

The next step of this project is to remove the dependency of ICE.
Initiatively, users are limited to my old colleague and they are using the same technology stack in their new job.
But more and more people are using this to upgrade an existing system, it is a hindering of adding ICE dependency.

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
