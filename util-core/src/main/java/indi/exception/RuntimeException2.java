package indi.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * 在原RuntimeException的基础上加了日志
 * 
 * 但日志记录的触发位置存在问题...
 */
@Slf4j
public class RuntimeException2 extends RuntimeException {
    private static final long serialVersionUID = 6896911585563375435L;

    private void init(String msg) {
        log.error(msg);
    }

    public RuntimeException2(String msg) {
        super(msg);
        init(msg);
    }
    
    public RuntimeException2(Throwable throwable) {
        super(throwable.getMessage());
        init(throwable.getMessage());
    }

    public RuntimeException2(String msg, Throwable throwable) {
        super(msg, throwable);
        init(msg);
    }

}
