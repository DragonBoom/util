package indi.exception;

import java.util.Optional;

import javax.annotation.Nullable;

import indi.data.Result;
import indi.data.Results;
import indi.directory.CommonExceptionDictionary;
import indi.util.StringUtils;

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
     * 将异常转化为Result。将对常见的异常进行翻译。
     */
    @Deprecated// 看起来挺美好，缺点也很明显，只适用于长期维护单个项目的场景；无法处理一个异常可能有多种解释的场景
    public static final Result<?> convert(Throwable e) {
        Throwable firstCause = findFirstCause(e);
        return Results.error(500,
                Optional.ofNullable(CommonExceptionDictionary.translate(firstCause)).orElse(firstCause.getMessage()));
    }
    
    /**
     * 将异常转化为简要的字符串，返回字符串包含指定深度的子异常。
     * 
     * <p>缺点也很明显，无法处理一个异常不同解释的场景，且所谓的关键字匹配其实与try...catch重复了；使用不够灵活
     * 
     * @param e 
     * @param maxDepth 遍历子异常的最大深度
     * @param keyword 用于匹配完整类名（包名+类名）的关键字；可为空，为空时直接取最新一条栈记录
     * @return
     */
    public static String stringify(Throwable e, int maxDepth, String keyword) {
        return stringify0(e, 0, maxDepth, keyword);
    }
    
    /**
     * 递归方法。将异常及其子异常转化为字符串。
     * 
     * @param e
     * @param depth
     * @param maxDepth
     * @param keyword 可为空
     * @return
     */
    private static String stringify0(Throwable e, int depth, int maxDepth, @Nullable String keyword) {
        Throwable cause = e.getCause();
        String causeStr = null;
        if (cause != null && depth < maxDepth) {
            causeStr = stringify0((Exception)cause, depth + 1, maxDepth, keyword);// ?? FIXME: bug!!
        }
        String newStr = stringifyExceptionByStackTrace(e, keyword);
        return StringUtils.isEmpty(causeStr) ? newStr : causeStr + " => " + newStr;
    }
    
    /**
     * 将某一异常，根据其栈记录转化为字符串。若异常没有栈记录，将直接返回异常的message。将跳过无法获取行数、文件名的代理类的记录。
     * 
     * @param e
     * @param keyword 用于匹配完整类名（包名+类名）的关键字；可为空，为空时直接取最新一条栈记录
     * @return 
     */
    private static String stringifyExceptionByStackTrace(Throwable e, @Nullable String keyword) {
        if (e == null) {
            return null;
        }
        String msg = e.getMessage();
        StackTraceElement[] stackTrace = e.getStackTrace();
        if (stackTrace == null || stackTrace.length == 0) {
            return msg;
        }
        // 取完整类名（包名+类名）含关键字的最新一条记录
        if (!StringUtils.isEmpty(keyword)) {
            for (StackTraceElement ste : stackTrace) {
                String className = ste.getClassName();
                String fileName = ste.getFileName();
                if (className != null && className.contains(keyword) && 
                        !"<generated>".equals(fileName)) {// 跳过文件名、行数无法识别的类（主要是代理）。。
                    // 这里少了完整类路径与方法名
                    return e.getMessage() + "(" + ste.getFileName() + ":" + ste.getLineNumber() + ")";
                }
            }
        }
        // 取最新一条记录
        StackTraceElement ste = stackTrace[0];// 获取栈最外一个元素，即最新一条栈记录
        return e.getMessage() + "(" + ste.getFileName() + ":" + ste.getLineNumber() + ")";// 该格式能被IDE解析成链接
    }
}
