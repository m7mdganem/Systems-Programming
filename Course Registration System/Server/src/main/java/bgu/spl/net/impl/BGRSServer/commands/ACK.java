package bgu.spl.net.impl.BGRSServer.commands;

import bgu.spl.net.api.Command;
import bgu.spl.net.impl.BGRSServer.CommandMessagingProtocol;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ACK implements Command<String> {


    private final short opcode; //represents the opcode of this command
    private final short responseMessage; //represents to which command this ack was sent for
    private String attachedMessage = null; //an optional string to attach to the ACK
    private int  numOfAttachments;

    public ACK(short responseMessage){
        opcode = 12;
        this.responseMessage = responseMessage;
        this.numOfAttachments = 1;
    }

    @Override
    public boolean addAttachment(String attachment) {
        if(!isDone()) {
            attachedMessage = attachment;
            numOfAttachments--;
            return true;
        }
        return false;
    }

    @Override
    public String getArgumentsType() {
        return "String";
    }

    @Override
    public Command execute(CommandMessagingProtocol protocol) {
        return null;
    }

    @Override
    public boolean isDone() {
        return numOfAttachments==0;
    }

    /**
     * Encodes the ACK command to an array of bytes (byte-buffer)
     * all using UTF-8.
     * <P>
     * @return bytes array represents the encoding of the ACK command
     */
    public byte[] encode(){
        byte[] buffer = new byte[1 << 10]; //start with 8k
        int len = 0;
        //encode the opcode of an acknowledgment message to bytes and add them to the buffer
        buffer[0] = (byte)((opcode >> 8) & 0xFF);
        buffer[1] = (byte)(opcode & 0xFF);
        //encode the responseMessage opcode to bytes and add them to the buffer
        buffer[2] = (byte)((responseMessage >> 8) & 0xFF);
        buffer[3] = (byte)(responseMessage & 0xFF);
        len = len+4;
        if(attachedMessage != null) {
            //encode the attached string using UTF-8 if it exists
            byte[] bytes = ("\n"+attachedMessage + "\0").getBytes(StandardCharsets.UTF_8);
            for (byte x : bytes) {
                if (len >= buffer.length)
                    buffer = Arrays.copyOf(buffer, len * 2);
                buffer[len++] = x;
            }
        }
        //copy the encoding to a new bytes array of the appropriate size
        byte[] output = new byte[len];
        for (int i=0; i<len; i++){
            output[i] = buffer[i];
        }
        return output;
    }

}
