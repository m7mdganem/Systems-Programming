#include <stdlib.h>
#include "../include/connectionHandler.h"
#include "KeyboardInputThread.h"
#include <boost/thread.hpp>

/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/
int main (int argc, char *argv[]) {
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host = argv[1];
    short port = atoi(argv[2]);

    ConnectionHandler connectionHandler(host, port);

    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }

    /*
     * The following two boolean variables will help us to decide when to stop running the threads.
     * the first variable will indicate that we received an answer for the LOGOUT command that we sent,
     * and the second one will indicate if the answer was Acknowledgment (=true) or Error (=false),
     * and if we received an ACK for a LOGOUT command we must stop.
     */
    static std::atomic_bool gotAnswerForLOGOUT(false);
    static std::atomic_bool shouldTerminate(false);

    KeyboardInputThread keyboardReader(connectionHandler,gotAnswerForLOGOUT,shouldTerminate);
    boost::thread keyboardThread(boost::bind(&KeyboardInputThread::run, &keyboardReader));

    while (!shouldTerminate) { //if we did not recieved an ACK upon a LOGOUT yet
        std::string line;
        if (connectionHandler.getLine(line)) {//get a line from the socket
            char const *s = line.c_str();
            short opcode = connectionHandler.bytesToShort(s, 0, 1);
            short responseOpcode = connectionHandler.bytesToShort(s, 2, 3);
            if (opcode == 13) {
                //if the message is ERROR then we dont need to write additional message to the client
                std::cout << "ERROR " << responseOpcode << std::endl;
                if (responseOpcode == 4) {
                    //if that is an answer for a LOGOUT, then we indicate that we received an answer
                    //and it is a ERROR so we should not terminate/stop.
                    gotAnswerForLOGOUT = true;
                    shouldTerminate = false;
                }
            } else if (opcode == 12 && (responseOpcode == 1 || responseOpcode == 2 ||
                                        responseOpcode == 3 || responseOpcode == 4 ||
                                        responseOpcode == 5 || responseOpcode == 10)) {
                //In these cases we dont need to write additional message to the client
                std::cout << "ACK " << responseOpcode << std::endl;
                if (responseOpcode == 4) {
                    //if that is an answer for a LOGOUT, then we indicate that we received an answer
                    //and it is an ACK so we should terminate/stop.
                    gotAnswerForLOGOUT = true;
                    shouldTerminate = true;
                }
            } else {
                //else, we write the additional message to the client
                std::cout << "ACK " << responseOpcode << line.substr(4, line.size()) << std::endl;
            }
        }else{
            //if we did not succeed to read from the socket, then the socket is closed in the servers side
            //in this case we will stop.
            break;
        }

    }

    keyboardThread.join();

    return 0;
}
