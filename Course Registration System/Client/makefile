CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system
BTFLAGS:=-lpthread -lboost_thread

all: BGRSclient
	g++ -o bin/BGRSclient bin/KeyboardInputThread.o bin/connectionHandler.o bin/BGRSclientMain.o $(LDFLAGS) $(BTFLAGS)

BGRSclient: bin/KeyboardInputThread.o bin/connectionHandler.o bin/BGRSclientMain.o
	
bin/connectionHandler.o: src/connectionHandler.cpp
	g++ $(CFLAGS) -o bin/connectionHandler.o src/connectionHandler.cpp

bin/BGRSclientMain.o: src/BGRSclientMain.cpp
	g++ $(CFLAGS) -o bin/BGRSclientMain.o src/BGRSclientMain.cpp
	
bin/KeyboardInputThread.o: src/KeyboardInputThread.cpp
	g++ $(CFLAGS) -o bin/KeyboardInputThread.o src/KeyboardInputThread.cpp

.PHONY: clean
clean:
	rm -f bin/*
