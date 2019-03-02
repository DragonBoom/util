package indi.util;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerUtils {
    private static final Logger logger = LoggerFactory.getLogger(LoggerUtils.class);

    
    /**
     * 获取Logger，虽然Logger的name是该类，但根据log4j的配置，输出的日志记录的仍是调用logger的类与相应的行数
     */
    @Deprecated
    public static final Logger getLogger() {
        return logger;
    }

    public static final void error(String reason) {
        logger.error(reason);
        throw new RuntimeException(reason);
    }
    
    @Deprecated
    public static final void info(String format, Object... args) {
    	logger.info(format, args);
    }
    
    @Deprecated
    public static final void debug(String format, Object... args) {
    	logger.debug(format, args);
    }
    
    @Deprecated
    public static final void error(String format, Object... args) {
    	logger.error(format, args);
    }
    
//    public static String getCallerClass() {
//    	String callerClassName = null;
//        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
//        
//        for (int i = 0; i < stackTrace.length; i++) {
//        	System.out.println(stackTrace[i].getClassName());
//            if (LoggerUtils.class.getName().equals(stackTrace[i].getClassName())) {
//            	System.out.println(stackTrace[i + 2].getClassName());
//                return stackTrace[i + 2].getClassName();
//            }
//        }
//        throw new RuntimeException(
//        		"Class Not Found By Stack Trace : " + Arrays.asList(stackTrace));
//    }

}
