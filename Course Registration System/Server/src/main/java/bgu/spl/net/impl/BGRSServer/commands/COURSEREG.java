package bgu.spl.net.impl.BGRSServer.commands;

import bgu.spl.net.api.Command;
import bgu.spl.net.impl.BGRSServer.Database;
import bgu.spl.net.impl.BGRSServer.CommandMessagingProtocol;
import bgu.spl.net.impl.BGRSServer.staff.Student;
import bgu.spl.net.api.User;

public class COURSEREG implements Command<Short> {

    private final short opcode; //represents the opcode of this command
    private short courseNum; //to save the course number supplied
    private int  numOfAttachments;
    private final Database database; //instance of the Database singleton

    public COURSEREG(){
        opcode = 5;
        numOfAttachments = 1;
        database = Database.getInstance();
    }

    @Override
    public boolean addAttachment(Short attachment) {
        if(!isDone()) {
            courseNum = attachment;
            numOfAttachments--;
            return true;
        }
        return false;
    }

    @Override
    public Command execute(CommandMessagingProtocol protocol) {
        if(isDone() && database.courseExistes(courseNum)) {
            String username = protocol.getUsername(); //get the client's username
            User profile = database.getProfile(username);//get the user's profile

            if (profile instanceof Student && profile.isLoggedIn()) {//if the user is a student and he is logged in
                    if (database.registerToCourse((int)courseNum, username)) {
                        //if the registration passed successfully, return ACK.
                        return new ACK(opcode);
                    }
            }
        }
        //if the registration failed, return ACK.
        return new ERR(opcode);
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
