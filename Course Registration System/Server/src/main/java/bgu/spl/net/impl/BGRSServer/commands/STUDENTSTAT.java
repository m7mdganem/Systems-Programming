package bgu.spl.net.impl.BGRSServer.commands;

import bgu.spl.net.api.Command;
import bgu.spl.net.impl.BGRSServer.Database;
import bgu.spl.net.impl.BGRSServer.CommandMessagingProtocol;
import bgu.spl.net.impl.BGRSServer.staff.Admin;
import bgu.spl.net.impl.BGRSServer.staff.Student;
import bgu.spl.net.api.User;

public class STUDENTSTAT implements Command<String> {

    private final short opcode; //represents the opcode of this command
    private String username; //to save the username supplied
    private int  numOfAttachments;
    private final Database database; //instance of the Database singleton

    public STUDENTSTAT(){
        this.opcode = 8;
        numOfAttachments = 1;
        database = Database.getInstance();
    }

    @Override
    public Command execute(CommandMessagingProtocol protocol) {
        if (isDone()){
            String username = protocol.getUsername(); //get the client's username
            User profile = database.getProfile(username); //get user's profile

            Student studentProfile = (Student) database.getProfile(this.username); //get student's profile

            //if student's profile exists (the student is registered to the database), and the
            //user who sent this command is an admin, and the admin is logged in
            if (studentProfile != null && profile instanceof Admin && profile.isLoggedIn()){
                String coursesOutputString = studentProfile.getRegisteredCoursesOrdered();

                String response = "Student: " + this.username + "\n"
                                  + "Courses: " + coursesOutputString;
                //if the command passed successfully return ACK.
                ACK ack = new ACK(opcode);
                ack.addAttachment(response);
                return ack;
            }
        }
        //if the command failed return ERROR.
        return new ERR(opcode);
    }

    @Override
    public boolean addAttachment(String attachment){
        if(!isDone()) {
            username = attachment;
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
    public boolean isDone() {
        return numOfAttachments==0;
    }
}
