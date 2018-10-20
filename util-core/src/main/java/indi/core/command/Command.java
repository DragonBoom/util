package indi.core.command;

public interface Command {
    
    String getName();

    CommandResult execute(CommandContext ctx) throws Throwable;

}
