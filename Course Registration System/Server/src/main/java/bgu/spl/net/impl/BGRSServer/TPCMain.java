package bgu.spl.net.impl.BGRSServer;

import bgu.spl.net.srv.Server;

public class TPCMain {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("You must enter at least 1 arguments!");
            return;
        }
        Database database = Database.getInstance();
        database.initialize("./Courses.txt");

        Integer port = Integer.parseInt(args[0]);
        Server.threadPerClient(port ,()->new CommandMessagingProtocol(), ()->new CommandMessgaeEncodeDecoder()).serve();
    }
}
