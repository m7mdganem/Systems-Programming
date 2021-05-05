#include "connectionHandler.h"

using boost::asio::ip::tcp;
using std::cin;
using std::cout;
using std::cerr;
using std::endl;
using std::string;

ConnectionHandler::ConnectionHandler(string host, short port): host_(host), port_(port), io_service_(), socket_(io_service_){}

ConnectionHandler::~ConnectionHandler() {
    close();
}

bool ConnectionHandler::connect() {
    std::cout << "Starting connect to "
              << host_ << ":" << port_ << std::endl;
    try {
        tcp::endpoint endpoint(boost::asio::ip::address::from_string(host_), port_); // the server endpoint
        boost::system::error_code error;
        socket_.connect(endpoint, error);
        if (error)
            throw boost::system::system_error(error);
    }
    catch (std::exception& e) {
        std::cerr << "Connection failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::getBytes(char bytes[], unsigned int bytesToRead) {
    size_t tmp = 0;
    boost::system::error_code error;
    try {
        while (!error && bytesToRead > tmp ) {
            tmp += socket_.read_some(boost::asio::buffer(bytes+tmp, bytesToRead-tmp), error);
        }
        if(error)
            throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::sendBytes(const char bytes[], int bytesToWrite) {
    int tmp = 0;
    boost::system::error_code error;
    try {
        while (!error && bytesToWrite > tmp ) {
            tmp += socket_.write_some(boost::asio::buffer(bytes + tmp, bytesToWrite - tmp), error);
        }
        if(error)
            throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::getLine(std::string& line) {
    return getFrameAscii(line, '\0');
}

bool ConnectionHandler::sendLine(std::string& line) {
    return sendFrameAscii(line, '\0');
}


bool ConnectionHandler::getFrameAscii(std::string& frame, char delimiter) {
    char ch;
    int len = 0;
    short opcode;
    short responseOpcode;
    bool gotOpcodes = false;

    try {
        do{
            if(!getBytes(&ch, 1)) { //if we failed to read from the socket
                return false;
            }
            if(ch != '\0' || !gotOpcodes) {
                frame.append(1, ch);
                len++;
            }
            if(len==2){
                /*
                 * if the len reached exactly 2, then we read the bytes which represents the response opcode
                 * from the server (ACK/ERROR), thus we convert it to short and save it.
                 */
                char const* c = frame.c_str();
                opcode = bytesToShort(c,0,1);
            }else if(len==4){
                /*
                 * if the len reached exactly 4, then we read the bytes which represents the message
                 * we got the response for, thus we convert it to short and save it and assign the gotOpcodes
                 * variable to true to indicate that we finished reading the response and we are working
                 * on the optional string message from now on.
                 */
                char const* c = frame.c_str();
                responseOpcode = bytesToShort(c,2,3);
                gotOpcodes = true;
            }
            if(len>=4 && opcode==13){ //if the message is ERR then stop
                break;
            }else if(len>=4 && opcode==12 &&( responseOpcode==1 || responseOpcode==2 ||
                                              responseOpcode==3 || responseOpcode==4 ||
                                              responseOpcode==5 || responseOpcode==10)){
                //if the message is ACK and its a response to a command that
                //does not need a reply, then stop
                break;
            }
        }while (delimiter != ch || !gotOpcodes);
    } catch (std::exception& e) {
        std::cerr << "recv failed2 (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}


bool ConnectionHandler::sendFrameAscii(const std::string& frame, char delimiter) {
    bool result=sendBytes(frame.c_str(),frame.length());
    if(!result) return false;
    return sendBytes(&delimiter,1);
}

// Close down the connection properly.
void ConnectionHandler::close() {
    try{
        socket_.close();
    } catch (...) {
        std::cout << "closing failed: connection already closed" << std::endl;
    }
}

short ConnectionHandler::bytesToShort(char const* bytesArr,int index1, int index2)
{
    short result = (short)((bytesArr[index1] & 0xff) << 8);
    result += (short)(bytesArr[index2] & 0xff);
    return result;
}
