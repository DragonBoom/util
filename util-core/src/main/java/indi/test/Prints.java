package indi.test;

import java.util.Arrays;
import java.util.List;

/**
 * 用于快速打印内容的工具类
 * 
 * @author wzh
 * @since 2019.12.16
 */
public class Prints {

    
    /**
     * 简单模仿logger的日志方式。支持{}参数。可考虑实现print(String, Object)与print(String, Object,
     * Object)方法以减少自动将对象转化为数组的消耗
     * 
     * @param content
     * @param args 如果需要传递数组，请使用printArray方法
     */
    public static void print(String content, Object...args) {
        // 采用String.format实现
        Object[] strArgs = Arrays.stream(args).map(Object::toString).toArray();// 字符串化以匹配%s
        content = content.replace("{}", "%s");
        System.out.println(String.format(content, strArgs));
    }
    
    /**
     * 为了不混淆是要传入单个数组还是传入多个参数，使用专门的方法来处理需要传入单个数组作为参数的情况
     * 
     * @param <T>
     * @param content
     * @param arg 
     */
    public static <T> void printArray(String content, T[] arg) {
        // T[] -> List<T>
        // 一个是Array没有实现toString方法，一个是会被String.format误认为是传入多个参数
        List<T> listArg = Arrays.asList(arg);
        // 采用String.format实现
        content = content.replace("{}", "%s");
        System.out.println(String.format(content, listArg));
    }
    
    public static <T> void printArray(T[] array) {
        System.out.println(Arrays.toString(array));
    }
}
