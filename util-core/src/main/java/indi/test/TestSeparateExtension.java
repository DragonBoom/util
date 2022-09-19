package indi.test;

import java.util.Date;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import indi.exception.ExceptionUtils;

/**
 * 用@ExtendWith引入该插件以为每个单元测试添加分隔符
 * 
 * @author DragonBoom
 *
 */
public class TestSeparateExtension
        implements BeforeTestExecutionCallback, AfterTestExecutionCallback, BeforeAllCallback, AfterAllCallback {
    private static final String SEPARATOR = "---------------------------------";
    private static final String BEGIN_ALL = "begin test";
    private static final String AFTER_ALL = "over test";
    private ThreadLocal<Date> beginDateThreadLocal = new ThreadLocal<>();

    @Override
    public void beforeTestExecution(ExtensionContext ctx) throws Exception {
        beginDateThreadLocal.set(new Date());
  
        String displayName = ctx.getDisplayName();
        Class<?> requiredTestClass = ctx.getRequiredTestClass();
        
        System.out.println(new StringBuilder(SEPARATOR)
                .append("Begin Test: ")
                .append(requiredTestClass.getSimpleName()).append(".").append(displayName)
                .toString());
    }

    @Override
    public void afterTestExecution(ExtensionContext ctx) throws Exception {
        Date now = new Date();
        Date beginDate = beginDateThreadLocal.get();
        beginDateThreadLocal.remove();
        
        // 打印异常信息
        ctx.getExecutionException().ifPresent(e -> System.err.println(ExceptionUtils.stringify(e, 5, null)));
        // 计算花费时间
        long duration = now.getTime() - beginDate.getTime();
        String timeDesc = null;
        if (duration > 1000) {
            timeDesc = new StringBuilder().append(duration/1000).append(" s").toString();
        } else {
            timeDesc = new StringBuilder().append(duration).append(" millis").toString();
        }
        System.out.println(new StringBuilder(SEPARATOR).append(" Test Over, Use ").append(timeDesc).toString());
        System.out.println();// 空一行
    }
    
    @Override
    public void beforeAll(ExtensionContext ctx) throws Exception {
        System.out.println(BEGIN_ALL);
    }

    @Override
    public void afterAll(ExtensionContext arg0) throws Exception {
        System.out.println(AFTER_ALL);
    }


}
