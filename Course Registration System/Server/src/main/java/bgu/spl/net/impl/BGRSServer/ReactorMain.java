package bgu.spl.net.impl.BGRSServer;

import bgu.spl.net.srv.Reactor;

public class ReactorMain {
    public static void main(String[] args) {
        if(args.length < 2) {
            System.out.println("You must enter at least 2 arguments!");
            return;
        }

        Database database = Database.getInstance();
        database.initialize("./Courses.txt");

        int port = Integer.parseInt(args[0]);
        int numOfThreads = Integer.parseInt(args[1]);
        new Reactor(numOfThreads,port,()->new CommandMessagingProtocol(),
                                      ()->new CommandMessgaeEncodeDecoder()).serve();
    }
}
