package indi.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * 用于将try catch 捕获到的Exception转化为RuntimeException，并进行统一的处理
 * 
 * 将会记录日志
 * 
 * @author DragonBoom
 *
 */
@Slf4j
public class WrapperException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private void init(Throwable e) {
        log.error("", e);// 直接将异常栈记录到error日志中
    }

    private void init(String msg) {
        log.error(msg);
    }

    public WrapperException(String msg) {
        super(msg);
        init(msg);
    }

    /**
     * 将打印异常栈信息
     * 
     * @param throwable
     */
    public WrapperException(Throwable throwable) {
        super(throwable);
//        throwable.printStackTrace();
        init(throwable);
    }

    /**
     * 将打印异常栈信息
     * 
     * @param msg
     * @param throwable
     */
    public WrapperException(String msg, Throwable throwable) {
        super(msg, throwable);
        throwable.printStackTrace();
        init(msg);
    }
}
