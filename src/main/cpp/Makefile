
ICE_HOME?=/opt/Ice-3.5
LOG4CPLUS_HOME?=/opt/log4cplus

CPPFLAGS=-I. -I$(ICE_HOME)/include -I$(LOG4CPLUS_HOME)/include
LDFLAGS=-L$(ICE_HOME)/lib64 -Wl,-rpath,$(ICE_HOME)/lib64 -L$(LOG4CPLUS_HOME)/lib64 -Wl,-rpath,$(LOG4CPLUS_HOME)/lib64 -llog4cplus -lIceXML -lIceUtil -lIce
CXX=g++ -g

all: dbpool_server

.PHONY: clean
clean :
	rm -f dbpool_server dbpool.h dbpool.cpp *.o

dbpool_server: dbpool.o dbpool_server.o main.o
	$(CXX) -o $@ $^ $(LDFLAGS)

dbpool.h: ../slice/dbpool.ice
	@echo "Generating Slice"
	$(ICE_HOME)/bin/slice2cpp -I$(ICE_HOME)/slice ../slice/dbpool.ice

dbpool.cpp: ../slice/dbpool.ice
	@echo "Generating Slice"
	$(ICE_HOME)/bin/slice2cpp -I$(ICE_HOME)/slice ../slice/dbpool.ice
