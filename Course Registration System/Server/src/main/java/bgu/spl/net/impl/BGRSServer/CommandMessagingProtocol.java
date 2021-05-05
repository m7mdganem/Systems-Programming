package bgu.spl.net.impl.BGRSServer;

import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.impl.BGRSServer.commands.ACK;
import bgu.spl.net.api.Command;
import bgu.spl.net.impl.BGRSServer.commands.LOGOUT;

public class CommandMessagingProtocol implements MessagingProtocol<Command> {

    /*
     * This username String represents the username of the client.
     * when the client logs in to a profile, we update this username
     * to be the username of the profile, and when he logs out we return it to null.
     * More formally, this field==null iff the client is not signed in to any account/profile.
     */
    private String username = null;
    private boolean shouldTerminate = false;

    public Command process(Command msg){
        if(msg instanceof LOGOUT){
            Command out = msg.execute(this);
            if(out instanceof ACK) //if the server return ACK then we should terminate the session with the client
                shouldTerminate = true;
            return out;
        }
        return msg.execute(this);
    }


    public boolean shouldTerminate(){
        return shouldTerminate;
    }

    public String getUsername(){
        return username;
    }

    public void setUsername(String username){
        this.username = username;
    }

}
