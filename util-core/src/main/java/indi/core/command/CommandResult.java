package indi.core.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class CommandResult {
    @Getter
    private NextStep nextStep;

    public enum NextStep {
        /**
         * 继续
         */
        CONTINUE,
        /**
         * 中止
         */
        TERMINATE,
        /**
         * 结束
         */
        END
    }
}
