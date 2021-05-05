package bgu.spl.net.impl.BGRSServer.commands;

import bgu.spl.net.api.Command;
import bgu.spl.net.impl.BGRSServer.CommandMessagingProtocol;

public class ERR implements Command<Boolean> {

    private final short opcode; //represents the opcode of this command
    private final short responseMessage; //represents to which command this ack was sent for

    public ERR(short responseMessage){
        opcode = 13;
        this.responseMessage = responseMessage;
    }

    @Override
    public boolean addAttachment(Boolean attachment) {
        return false;
    }

    @Override
    public String getArgumentsType() {
        return null;
    }

    @Override
    public Command execute(CommandMessagingProtocol protocol) {
        return null;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    public byte[] encode(){
        byte[] buffer = new byte[4]; //start with 8k
        //encode the opcode of an acknowledgment message to bytes and add them to the buffer
        buffer[0] = (byte)((opcode >> 8) & 0xFF);
        buffer[1] = (byte)(opcode & 0xFF);
        //encode the responseMessage opcode to bytes and add them to the buffer
        buffer[2] = (byte)((responseMessage >> 8) & 0xFF);
        buffer[3] = (byte)(responseMessage & 0xFF);
        return buffer;
    }
}
