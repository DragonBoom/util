package indi.core.command;

import java.util.List;
import java.util.Objects;

import com.google.common.collect.ImmutableList;

import indi.core.command.CommandResult.NextStep;
import lombok.Getter;
import lombok.Setter;

public class CommandPipeline {
    @Getter
    private List<Command> commands;
    @Setter
    private CommandExceptionHandler exceptionHandler;

    public CommandPipeline(List<Command> commands) {
        this.commands = commands;
    }

    public CommandResult process(final CommandContext ctx) throws Throwable {
        Objects.requireNonNull(ctx);
        CommandResult commandResult = null;
        for (Command command : commands) {
            // 执行命令
            try {
                commandResult = command.execute(ctx);
            } catch (Throwable e) {
                if (exceptionHandler != null) {
                    exceptionHandler.handler(command, ctx);
                } else {
                    throw e;
                }
            }
            Objects.requireNonNull(commandResult);
    
            // 根据命令结果，判断下一步该如何处理
            NextStep nextStep = commandResult.getNextStep();
            Objects.requireNonNull(nextStep);
            switch (nextStep) {
            case CONTINUE:
                break;
            default:
                return commandResult;
            }
        }
        return commandResult;
    }

    public static class Builder {
        ImmutableList.Builder<Command> builder = ImmutableList.builder();

        public Builder add(Command command) {
            builder.add(command);
            return this;
        }

        public CommandPipeline build() {
            return new CommandPipeline(builder.build());
        }
    }
}
