package indi.core.exception;

import indi.core.util.LoggerUtils;

/**
 * 在原RuntimeException的基础上加了日志
 */
public class RuntimeException2 extends RuntimeException {
    private static final long serialVersionUID = 6896911585563375435L;

    private void init(String msg) {
        LoggerUtils.getLogger().error(msg);
    }

    public RuntimeException2(String msg) {
        super(msg);
        init(msg);
    }

    public RuntimeException2(String msg, Throwable throwable) {
        super(msg, throwable);
        init(msg);
    }

}
