package bgu.spl.net.impl.BGRSServer.commands;

import bgu.spl.net.api.Command;
import bgu.spl.net.impl.BGRSServer.Database;
import bgu.spl.net.impl.BGRSServer.CommandMessagingProtocol;
import bgu.spl.net.impl.BGRSServer.staff.Student;
import bgu.spl.net.api.User;

public class MYCOURSES implements Command<Short> {

    private final short opcode; //represents the opcode of this command
    private final Database database; //instance of the Database singleton

    public MYCOURSES(){
        opcode = 11;
        database = Database.getInstance();
    }

    @Override
    public Command execute(CommandMessagingProtocol protocol) {
        if (isDone()){
            String username = protocol.getUsername(); //get the username of the client
            User profile = database.getProfile(username); //get the user's profile
            if(profile instanceof Student && profile.isLoggedIn()){
                //we use replaceAll("\\s","") to remove all redundant spaces that the toString method creates
                String response = ((Student) profile).getRegisteredCourses().toString().replaceAll("\\s","");
                ACK ack = new ACK(opcode);
                ack.addAttachment(response);
                return ack;
            }
        }
        return new ERR(opcode);
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public boolean addAttachment(Short attachment) {
        return false;
    }

    @Override
    public String getArgumentsType() {
        return "Short";
    }
}
