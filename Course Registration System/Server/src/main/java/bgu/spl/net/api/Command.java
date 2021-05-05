package bgu.spl.net.api;

import bgu.spl.net.impl.BGRSServer.CommandMessagingProtocol;

public interface Command<T> {

    /**
     * This method adds the specified {@code attachment} to the
     * command, for example if the command requires an attachment/argument
     * we supply it to this method and it saves it to the command.
     * <p>
     * @param attachment required argument for the command
     * @return true if the attachment added successfully and false otherwise
     */
    boolean addAttachment(T attachment);

    /**
     * Retrieves the command attachments type as string.
     * <P>
     * @return A string represents the type of the command attachments.
     */
    String getArgumentsType();

    /**
     * This method executes the command and applies all the
     * necessary operation to complete the command.
     * <p>
     * @param protocol The protocol which will execute this command
     * @return The command the server should return to the client.
     */
    Command execute(CommandMessagingProtocol protocol);

    /**
     * Retrieves the status of the command construction.
     * <p>
     * @return true if the command is fully constructed
     */
    boolean isDone();
}
