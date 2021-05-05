package bgu.spl.net.impl.BGRSServer.commands;

import bgu.spl.net.api.Command;
import bgu.spl.net.impl.BGRSServer.Database;
import bgu.spl.net.impl.BGRSServer.CommandMessagingProtocol;
import bgu.spl.net.impl.BGRSServer.staff.Student;
import bgu.spl.net.api.User;

public class UNREGISTER implements Command<Short> {

    private final short opcode; //represents the opcode of this command
    private short courseNum; //to save the course number supplied
    private int  numOfAttachments;
    private final Database database; //instance of the Database singleton

    public UNREGISTER(){
        opcode = 10;
        numOfAttachments = 1;
        database = Database.getInstance();
    }

    @Override
    public Command execute(CommandMessagingProtocol protocol) {
        if (isDone() & database.courseExistes(courseNum)){
            String username = protocol.getUsername(); //get the client's username
            User profile = database.getProfile(username); //get user's profile

            if(profile instanceof Student && profile.isLoggedIn()){
                if(database.unregisterToCourse((int) courseNum, username))
                    //if we unregister successfully, return ACK.
                    return new ACK(opcode);
            }
        }
        //if we fail to unregister, return ERROR.
        return new ERR(opcode);
    }

    @Override
    public boolean addAttachment(Short attachment) {
        courseNum = attachment;
        numOfAttachments--;
        return true;
    }

    @Override
    public boolean isDone() {
        return numOfAttachments==0;
    }

    @Override
    public String getArgumentsType() {
        return "Short";
    }
}
