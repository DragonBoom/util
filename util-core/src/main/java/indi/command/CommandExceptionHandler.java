package indi.command;

public interface CommandExceptionHandler {

    CommandResult handler(Command command, CommandContext ctx);
}
