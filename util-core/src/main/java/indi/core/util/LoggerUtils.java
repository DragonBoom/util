package indi.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerUtils {
    private static final Logger logger = LoggerFactory.getLogger(LoggerUtils.class);

    /**
     * 获取Logger，虽然Logger的name是该类，但根据log4j的配置，输出的日志记录的仍是调用logger的类与相应的行数
     */
    public static final Logger getLogger() {
        return logger;
    }

    public static final void errorAndThrow(String reason) {
        logger.error(reason);
        throw new RuntimeException(reason);
    }
    
    
    /**
     * String callerClasszName = null;
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        
        for (int i = 0; i < stackTrace.length; i++) {
            if (LoggerUtils.class.getName().equals(stackTrace[i].getClassName())) {
                callerClasszName = stackTrace[i + 1].getClassName();
            }
        }
        if (callerClasszName == null) {
            throw new RuntimeException(
                    "Class Not Found By Stack Trace : " + Arrays.asList(stackTrace));
        }
     */

}
