package indi.core.command;

public interface CommandExceptionHandler {

    CommandResult handler(Command command, CommandContext ctx);
}
