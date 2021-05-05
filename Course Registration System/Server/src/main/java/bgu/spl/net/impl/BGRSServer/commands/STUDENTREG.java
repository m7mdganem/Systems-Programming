package bgu.spl.net.impl.BGRSServer.commands;

import bgu.spl.net.api.Command;
import bgu.spl.net.impl.BGRSServer.CommandMessagingProtocol;
import bgu.spl.net.impl.BGRSServer.Database;

import java.util.LinkedList;
import java.util.List;

public class STUDENTREG implements Command<String> {

    private final short opcode; //represents the opcode of this command
    private final List<String> attachedMessages; //to save the username and password supplied
    private int  numOfAttachments;
    private final Database database; //instance of the Database singleton

    public STUDENTREG() {
        opcode = 2;
        numOfAttachments = 2;
        attachedMessages = new LinkedList<>();
        database = Database.getInstance();
    }

    @Override
    public boolean addAttachment(String attachment) {
        if(!isDone()) {
            attachedMessages.add(attachment);
            numOfAttachments--;
            return true;
        }
        return false;
    }

    @Override
    public Command execute(CommandMessagingProtocol protocol) {
        if(isDone()) {
            String username = protocol.getUsername(); //get the client's username
            if (username == null) { //if the client is not logged in to any account
                if (database.addStudent(attachedMessages.get(0), attachedMessages.get(1))) {
                    //if we success to register the student in the database, return ACK.
                    return new ACK(opcode);
                }
            }
        }
        //if we fail to register the student in the database, return ERROR.
        return new ERR(opcode);
    }

    @Override
    public boolean isDone() {
        return numOfAttachments==0;
    }

    @Override
    public String getArgumentsType() {
        return "String";
    }
}
