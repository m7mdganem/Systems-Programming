//
// Created by mohammad on 03/01/2021.
//

#ifndef CLIENT_KEYBOARDINPUTTHREAD_H
#define CLIENT_KEYBOARDINPUTTHREAD_H


class KeyboardInputThread {
private:
    ConnectionHandler& connectionHandler;
    std::atomic_bool& gotAnswerForLOGOUT;
    std::atomic_bool& shouldTerminate;

public:
    KeyboardInputThread(ConnectionHandler& connectionHandler,std::atomic_bool&,std::atomic_bool&);
    void run();
    bool getOutputString(std::string& line,std::string& outputString);
    void shortToBytes(short num, char* bytesArr);
};


#endif //CLIENT_KEYBOARDINPUTTHREAD_H
