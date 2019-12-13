package me.Cooltimmetje.Skuddbot.Commands;

import org.javacord.api.entity.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class CommandManager {

    private static Logger logger = LoggerFactory.getLogger(CommandManager.class);

    private ArrayList<Command> commands;

    public CommandManager(){
        commands = new ArrayList<>();
    }

    public void registerCommand(Command command){
        logger.info("Registering commmand " + command.toString() + " with invokers " + String.join(",", command.getInvokers()));
        commands.add(command);
    }

    public void process(Message message){
        String commandPrefix = ">>";
        String requestedInvoker = message.getContent().split(" ")[0].toLowerCase();
        if(!requestedInvoker.startsWith(commandPrefix)) return;
        requestedInvoker = requestedInvoker.substring(commandPrefix.length());
        for(Command command : commands){
            for(String invoker : command.getInvokers()){
                if (requestedInvoker.equals(invoker)) {
                    command.run(message);
                    return;
                }
            }
        }
    }

}
