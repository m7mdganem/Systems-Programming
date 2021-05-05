package bgu.spl.net.impl.BGRSServer.commands;

import bgu.spl.net.api.Command;
import bgu.spl.net.impl.BGRSServer.Database;
import bgu.spl.net.impl.BGRSServer.CommandMessagingProtocol;
import bgu.spl.net.impl.BGRSServer.staff.Admin;
import bgu.spl.net.api.User;

import java.util.concurrent.ConcurrentHashMap;

public class COURSESTAT implements Command<Short> {

    private final short opcode; //represents the opcode of this command
    private short courseNum; //to save the course number the client supplied
    private int  numOfAttachments;
    private final Database database; //instance of the Database singleton

    public COURSESTAT(){
        opcode = 7;
        numOfAttachments = 1;
        database = Database.getInstance();
    }

    @Override
    public boolean addAttachment(Short attachment) {
        courseNum = attachment;
        numOfAttachments--;
        return true;
    }

    @Override
    public String getArgumentsType() {
        return "Short";
    }

    @Override
    public Command execute(CommandMessagingProtocol protocol) {
        if(isDone() && database.courseExistes(courseNum)){
            String username = protocol.getUsername(); //get the client's username
            User profile = database.getProfile(username); //get the user's profile

            //get all the course information
            ConcurrentHashMap<String, Object> courseInfo = database.getCourseInfo((int) courseNum);

            if (courseInfo!=null && profile instanceof Admin && profile.isLoggedIn()) {

                //get the course info
                String courseName = (String) courseInfo.get("courseName"); //get the course name
                //get number of available seats and number of max students
                Integer numOfMaxStudents = (Integer) courseInfo.get("numOfMaxStudents");
                Integer numOfSeatsAvailable = database.getNumOfSeatsAvailable((int) courseNum);
                //get the string of the list of registered students sorted alphabetically
                String registeredStudentsString = database.getRegisteredStudentsOrderedString(courseNum);

                String response = "Course: (" + courseNum + ") " + courseName + "\n"
                        + "Seats Available: " + numOfSeatsAvailable + "/" + numOfMaxStudents + "\n"
                        + "Students Registered: " + registeredStudentsString;

                ACK ack = new ACK(opcode);
                ack.addAttachment(response);
                return ack;
            }
        }
        return new ERR(opcode);
    }

    @Override
    public boolean isDone() {
        return numOfAttachments==0;
    }
}
