#include <connectionHandler.h>
#include "../include/KeyboardInputThread.h"
#include <boost/thread.hpp>

KeyboardInputThread::KeyboardInputThread(ConnectionHandler& connectionHandler,
                                         std::atomic_bool& gotAnswerForLOGOUT,
                                         std::atomic_bool& shouldTerminate)
                            : connectionHandler(connectionHandler),
                              gotAnswerForLOGOUT(gotAnswerForLOGOUT),
                              shouldTerminate(shouldTerminate){

}

void KeyboardInputThread::run() {
    while (1){
            const short bufsize = 1024;
            char buf[bufsize];
            std::cin.getline(buf, bufsize); //read a line from the client/terminal
            std::string line(buf);//convert the line to string

            std::string output;

            /*
             * this method will take the line that we got from the keyboard, send
             * the appropriate command (opcode) to the server.
             * this function returns true iff the command that we sent need to send
             * other arguments to the server (e.g. username, password, etc...), if so
             * it writes these arguments to output, separated by spaces.
             */
            bool sendAttachedArguments = getOutputString(line,output);

            //if we need to send arguments besides the command (opcode) we iterate
            // over its chars and send them.
            if(sendAttachedArguments) {
                for (char s : output)
                    connectionHandler.sendBytes(&s, 1);
            }

            //if the command that we sent is LOGOUT, then we run in busy wait in this
            //loop until we get an answer from the server, if we got an acknowledgment to our
            //message, meaning we should terminate, we exit the method.
            if(line == "LOGOUT"){
                while(!gotAnswerForLOGOUT){
                }
                gotAnswerForLOGOUT = false;
                if(shouldTerminate){
                    return;
                }
            }
    }
}

bool KeyboardInputThread::getOutputString(std::string& line, std::string& outputString) {
    char opcodeBytes[2]; //the array that will hold our chars that represents the short which represents our opcode
    char courseOpcodeBytes[2]; //the array that will hold course number chars that represents the course number attached to the command, if needed

    if (line == "LOGOUT"){
        shortToBytes(4,opcodeBytes);

        connectionHandler.sendBytes(opcodeBytes,2);

        return false;

    }else if ((line.substr(0,line.find(" "))) == "ADMINREG"){
        shortToBytes(1,opcodeBytes);
        std::string sub = line.substr(line.find(" ")+1,line.size()-1);
        for(char s : sub){
            if(s == ' ')
                outputString = outputString + '\0';
            else
                outputString = outputString + s;
        }
        outputString = outputString + '\0';

        connectionHandler.sendBytes(opcodeBytes,2);

        return true;

    } else if ((line.substr(0,line.find(" "))) == "STUDENTREG"){
        shortToBytes(2,opcodeBytes);
        std::string sub = line.substr(line.find(" ")+1,line.size()-1);
        for(char s : sub){
            if(s == ' ')
                outputString = outputString + '\0';
            else
                outputString = outputString + s;
        }
        outputString = outputString + '\0';

        connectionHandler.sendBytes(opcodeBytes,2);

        return true;

    }else if ((line.substr(0,line.find(" "))) == "LOGIN"){
        shortToBytes(3,opcodeBytes);
        std::string sub = line.substr(line.find(" ")+1,line.size()-1);
        for(char s : sub){
            if(s == ' ')
                outputString = outputString + '\0';
            else
                outputString = outputString + s;
        }
        outputString = outputString + '\0';

        connectionHandler.sendBytes(opcodeBytes,2);

        return true;

    }else if ((line.substr(0,line.find(" "))) == "COURSEREG"){
        shortToBytes(5,opcodeBytes);

        std::string sub = line.substr(line.find(" ")+1,line.size()-1);
        short courseNum = std::stoi(sub); //we know that the course numbers are short, so this conversion is ok

        shortToBytes(courseNum,courseOpcodeBytes);

        connectionHandler.sendBytes(opcodeBytes,2);

        connectionHandler.sendBytes(courseOpcodeBytes,2);

        return false;

    }else if ((line.substr(0,line.find(" "))) == "KDAMCHECK"){
        shortToBytes(6,opcodeBytes);

        std::string sub = line.substr(line.find(" ")+1,line.size()-1);
        short courseNum = std::stoi(sub); //we know that the course numbers are short, so this conversion is ok

        shortToBytes(courseNum,courseOpcodeBytes);

        connectionHandler.sendBytes(opcodeBytes,2);

        connectionHandler.sendBytes(courseOpcodeBytes,2);

        return false;

    }else if ((line.substr(0,line.find(" "))) == "COURSESTAT"){
        shortToBytes(7,opcodeBytes);

        std::string sub = line.substr(line.find(" ")+1,line.size()-1);
        short courseNum = std::stoi(sub); //we know that the course numbers are short, so this conversion is ok

        shortToBytes(courseNum,courseOpcodeBytes);

        connectionHandler.sendBytes(opcodeBytes,2);

        connectionHandler.sendBytes(courseOpcodeBytes,2);

        return false;

    }else if ((line.substr(0,line.find(" "))) == "STUDENTSTAT"){
        shortToBytes(8,opcodeBytes);

        std::string sub = line.substr(line.find(" ")+1,line.size()-1);
        for(char s : sub){
            if(s == ' ')
                outputString = outputString + '\0';
            else
                outputString = outputString + s;
        }
        outputString = outputString + '\0';

        connectionHandler.sendBytes(opcodeBytes,2);

        return true;

    }else if ((line.substr(0,line.find(" "))) == "ISREGISTERED"){
        shortToBytes(9,opcodeBytes);

        std::string sub = line.substr(line.find(" ")+1,line.size()-1);
        short courseNum = std::stoi(sub); //we know that the course numbers are short, so this conversion is ok

        shortToBytes(courseNum,courseOpcodeBytes);

        connectionHandler.sendBytes(opcodeBytes,2);

        connectionHandler.sendBytes(courseOpcodeBytes,2);

        return false;

    }else if ((line.substr(0,line.find(" "))) == "UNREGISTER"){
        shortToBytes(10,opcodeBytes);

        std::string sub = line.substr(line.find(" ")+1,line.size()-1);
        short courseNum = std::stoi(sub); //we know that the course numbers are short, so this conversion is ok

        shortToBytes(courseNum,courseOpcodeBytes);

        connectionHandler.sendBytes(opcodeBytes,2);

        connectionHandler.sendBytes(courseOpcodeBytes,2);

        return false;

    }else if ((line.substr(0,line.find(" "))) == "MYCOURSES"){
        shortToBytes(11,opcodeBytes);
        connectionHandler.sendBytes(opcodeBytes,2);
        return false;
    }
    return false;
}

void KeyboardInputThread::shortToBytes(short num, char* bytesArr)
{
    bytesArr[0] = ((num >> 8) & 0xFF);
    bytesArr[1] = (num & 0xFF);
}

