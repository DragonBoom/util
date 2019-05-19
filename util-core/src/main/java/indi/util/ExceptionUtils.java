package indi.util;

import java.util.Optional;

import indi.data.RestResult;
import indi.directory.CommonExceptionDictionary;

/**
 * 用于处理异常类的工具
 * 
 * @author DragonBoom
 *
 */
public class ExceptionUtils {

    /**
     * 获取层层封装的异常链的起点
     */
    public static final Throwable findFirstCause(Throwable e) {
        Throwable cause = e.getCause();
        if (cause == null) {
            return e;
        } else {
            return findFirstCause(cause);
        }
    }

    /**
     * 将异常转化为RestResult。将对常见的异常进行翻译
     */
    public static final RestResult<?> convert(Throwable e) {
        Throwable firstCause = findFirstCause(e);
        return RestResult.asError(500,
                Optional.ofNullable(CommonExceptionDictionary.translate(firstCause)).orElse(firstCause.getMessage()));
    }
}
