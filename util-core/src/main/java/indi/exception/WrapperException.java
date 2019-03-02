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
	
	private void init(String msg) {
        log.error(msg);
    }
	
	public WrapperException(String msg) {
        super(msg);
        init(msg);
    }
	
	 public WrapperException(Throwable throwable) {
	        super(throwable);
	        init(throwable.getMessage());
	    }
	
	public WrapperException(String msg, Throwable throwable) {
        super(msg, throwable);
        init(msg);
    }
}
