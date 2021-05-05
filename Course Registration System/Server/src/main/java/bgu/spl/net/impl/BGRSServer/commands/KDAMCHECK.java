package bgu.spl.net.impl.BGRSServer.commands;

import bgu.spl.net.api.Command;
import bgu.spl.net.impl.BGRSServer.Database;
import bgu.spl.net.impl.BGRSServer.CommandMessagingProtocol;
import bgu.spl.net.impl.BGRSServer.staff.Student;
import bgu.spl.net.api.User;

import java.util.LinkedList;

public class KDAMCHECK implements Command<Short> {

    private final short opcode; //represents the opcode of this command
    private short courseNum; //to save the course number the client supplied
    private int  numOfAttachments;
    private final Database database; //instance of the Database singleton

    public KDAMCHECK(){
        opcode = 6;
        numOfAttachments = 1;
        database = Database.getInstance();
    }

    @Override
    public boolean addAttachment(Short attachment) {
        if (!isDone()) {
            courseNum = attachment;
            numOfAttachments--;
            return true;
        }
        return false;
    }



    @Override
    public Command execute(CommandMessagingProtocol protocol) {
        if (isDone() && database.courseExistes(courseNum)) {
            String username = protocol.getUsername(); //get the username of the client
            User profile = database.getProfile(username); //get the user's profile

            //if the username exists, and the user is a student and the student is logged in
            if (profile instanceof Student && profile.isLoggedIn()) {
                //get the kdam courses list
                LinkedList<Integer> kdam = (LinkedList<Integer>)database.getCourseInfo((int) courseNum).get("kdamCoursesList");

                //create the response
                ACK ack = new ACK(opcode);
                ack.addAttachment(kdam.toString());
                return ack;
            }
        }
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
