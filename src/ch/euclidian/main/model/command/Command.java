package ch.euclidian.main.model.command;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public interface Command
{
    String name();
    
    String info(String prefix);
    
    String verify(String prefix, String[] args, MessageReceivedEvent event);

    void action(String prefix, String[] args, MessageReceivedEvent event);
}