package bgu.spl.net.impl.BGRSServer.commands;

import bgu.spl.net.api.Command;
import bgu.spl.net.impl.BGRSServer.Database;
import bgu.spl.net.impl.BGRSServer.CommandMessagingProtocol;
import bgu.spl.net.api.User;

public class LOGOUT implements Command<String> {

    private final short opcode; //represents the opcode of this command
    private final Database database; //instance of the Database singleton

    public LOGOUT(){
        opcode = 4;
        database = Database.getInstance();
    }

    @Override
    public Command execute(CommandMessagingProtocol protocol) {
        if(isDone()) {
            String username = protocol.getUsername();//get the username of the client
            User profile = database.getProfile(username); //get the user's profile
            if (profile != null && profile.logout()) {
                protocol.setUsername(null); //set the protocols username to null
                //if profile exists and we logout successfully, return ACK.
                return new ACK(opcode);
            }
        }
        //if we fail to logout, return ERROR.
        return new ERR(opcode);
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public boolean addAttachment(String attachment) {
        return false;
    }

    @Override
    public String getArgumentsType() {
        return "String";
    }
}
