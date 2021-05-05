package bgu.spl.net.impl.BGRSServer.commands;

import bgu.spl.net.api.Command;
import bgu.spl.net.impl.BGRSServer.CommandMessagingProtocol;
import bgu.spl.net.impl.BGRSServer.Database;
import bgu.spl.net.api.User;

import java.util.LinkedList;
import java.util.List;

public class LOGIN implements Command<String> {

    private final short opcode; //represents the opcode of this command
    private final List<String> attachedMessages; //to save the username and password supplied
    private int  numOfAttachments;
    private final Database database; //instance of the Database singleton

    public LOGIN(){
        this.opcode = 3;
        numOfAttachments = 2;
        attachedMessages = new LinkedList<String>();
        database = Database.getInstance();
    }

    @Override
    public boolean addAttachment(String attachment){
        if(!isDone()) {
            attachedMessages.add(attachment);
            numOfAttachments--;
            return true;
        }
        return false;
    }

    @Override
    public Command execute(CommandMessagingProtocol protocol) {
        //if we got the full command with all the required arguments/attachments, and the client did
        //not log in to any profile yet
        if(isDone() && protocol.getUsername() == null) {
            String username = attachedMessages.get(0);//get the username supplied with the command
            String password = attachedMessages.get(1);//get the password supplied with the command
            User profile = database.getProfile(username);

            if (profile != null && profile.login(password)) { //get the profile of the user and try to login
                protocol.setUsername(username);
                //if login passed successfully return ACK.
                return new ACK(opcode);
            }
        }
        //if login failed return ERROR.
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
