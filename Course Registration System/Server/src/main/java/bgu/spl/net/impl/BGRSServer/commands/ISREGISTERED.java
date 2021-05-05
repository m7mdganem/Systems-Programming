package bgu.spl.net.impl.BGRSServer.commands;

import bgu.spl.net.api.Command;
import bgu.spl.net.impl.BGRSServer.Database;
import bgu.spl.net.impl.BGRSServer.CommandMessagingProtocol;
import bgu.spl.net.impl.BGRSServer.staff.Student;
import bgu.spl.net.api.User;

import java.util.LinkedList;

public class ISREGISTERED implements Command<Short> {

    private final short opcode; //represents the opcode of this command
    private short courseNum; //to save the course number the client supplied
    private int  numOfAttachments;
    private final Database database; //instance of the Database singleton

    public ISREGISTERED(){
        opcode = 9;
        numOfAttachments = 1;
        database = Database.getInstance();
    }

    @Override
    public Command execute(CommandMessagingProtocol protocol) {
        if (isDone() && database.courseExistes(courseNum)){

            String username = protocol.getUsername();//get the client's username
            User profile = database.getProfile(username); //get the user's profile

            if(profile instanceof Student && profile.isLoggedIn()){
                //get the courses of the student
                LinkedList<Integer> courses = ((Student) profile).getRegisteredCourses();
                if (courses.contains((int) this.courseNum)){
                    ACK ack = new ACK(opcode);
                    ack.addAttachment("REGISTERED");
                    return ack;
                }else{
                    ACK ack = new ACK(opcode);
                    ack.addAttachment("NOT REGISTERED");
                    return ack;
                }
            }
        }
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
