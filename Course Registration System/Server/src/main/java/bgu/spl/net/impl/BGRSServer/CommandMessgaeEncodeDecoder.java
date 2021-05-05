package bgu.spl.net.impl.BGRSServer;

import bgu.spl.net.api.Command;
import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.impl.BGRSServer.commands.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;


public class CommandMessgaeEncodeDecoder implements MessageEncoderDecoder<Command> {

    private byte[] buffer = new byte[1 << 10]; //start with 8k
    private int len = 0;
    private boolean buildingInProcess = false;
    private boolean gotOpcode = false;
    private Command command = null;

    @Override
    public Command decodeNextByte(byte nextByte) {
        /*
         * The \0 Byte is the separating byte which separates strings in the command, thus
         * we dont add it to the frame, except when it is part of the opcode or if the command
         * accepts arguments of type "short", in that case the 0 byte is part of the command.
         */
        if(nextByte != '\0' | !gotOpcode | (gotOpcode && command.getArgumentsType().equals("Short")))
            pushByte(nextByte);

        if(gotOpcode) {//if we got the op code, meaning if we know what the command is
            if (!buildingInProcess) {//if we did not build a new command yet
                command = createNewCommand();
                buildingInProcess = true;
                len = 0;
            }else if(command.getArgumentsType().equals("Short") & len==2){
                command.addAttachment(bytesToShort(buffer));
                len = 0;
            }else if(command.getArgumentsType().equals("String") & nextByte == '\0'){
                command.addAttachment(new String(buffer, 0, len, StandardCharsets.UTF_8));
                len = 0;
            }
            if(command.isDone()){
                return popMessage();
            }
        }
        return null;
    }

    @Override
    public byte[] encode(Command message) {
        //the server sends only ACK and ERROR messages back to clients
        if(message instanceof ACK){
            return ((ACK) message).encode();
        }else{
            return ((ERR) message).encode();
        }
    }

    private void pushByte (byte nextByte){
        if(len >= buffer.length)
            buffer = Arrays.copyOf(buffer,len*2);
        buffer[len] = nextByte;
        len++;
        if(len == 2)
            gotOpcode = true;
    }

    private Command popMessage(){
        gotOpcode = false;
        buildingInProcess = false;
        len = 0;
        return command;
    }

    private short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }

    private Command createNewCommand(){
        short opcode = bytesToShort(buffer);
        Command newCommand;
        switch (opcode){
            case 1:
                newCommand = new ADMINREG();
                break;
            case 2 :
                newCommand = new STUDENTREG();
                break;
            case 3 :
                newCommand = new LOGIN();
                break;
            case 4 :
                newCommand = new LOGOUT();
                break;
            case 5 :
                newCommand = new COURSEREG();
                break;
            case 6 :
                newCommand = new KDAMCHECK();
                break;
            case 7 :
                newCommand = new COURSESTAT();
                break;
            case 8 :
                newCommand = new STUDENTSTAT();
                break;
            case 9 :
                newCommand = new ISREGISTERED();
                break;
            case 10 :
                newCommand = new UNREGISTER();
                break;
            case 11 :
                newCommand = new MYCOURSES();
                break;
            case 12 :
                newCommand = new ACK((short) 0);
                break;
            default:
                newCommand = new ERR((short) 0);
                break;
        }
        return newCommand;
    }
}
