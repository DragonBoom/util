package indi.util;

import java.lang.Character.UnicodeBlock;
import java.util.Collection;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Ascii;
import com.google.common.base.CaseFormat;
import com.google.common.collect.Range;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class StringUtils {
    
    /**
     * 这段代码是随便从网上copy过来的，之后再看一下原理... TODO
     */
    public static boolean isChinese(Character c) {
        // TODO Optimise
        UnicodeBlock cb = Character.UnicodeBlock.of(c);
        return (cb == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || cb == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || cb == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || cb == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || cb == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || cb == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS);
    }

    
    public static boolean isChinese(String str) {
        for (Character c : str.toCharArray()) {
            boolean result = isChinese(c);
            if (!result) {
                return false;
            }
        }
        return true;
    }
    
    public static final boolean isEnglish(Character c) {
        Range<Character> r1 = Range.closed('a', 'z');
        Range<Character> r2 = Range.closed('A', 'Z');
        return r1.contains(c) || r2.contains(c);
    }

    /**
     * 用于匹配字符串中除了中文外的常见的字符
     */
    private static final Pattern pattern = Pattern.compile("[\\w\\.\\[\\]\\\\]【】");

    /**
     * 简单判断是否是乱码，若字符串不含中文且不是纯数字、英文、下划线等符号，就当作乱码
     * 
     * @return
     */
    public static boolean isChaos(String str) {
        // 只要有正常的中文、日文、韩文字符就当作不是乱码
        char[] chars = str.toCharArray();
        for (Character c : chars) {
            if (isChinese(c)) {
                return false;
            }
        }
        // 用正则表达式判断剩下的字符是否都是数字、英文或下划线等符号
        int count = str.length();
        Matcher matcher = pattern.matcher(str);
        // 有多少个字符，就应该匹配多少次；若有匹配不了这个次数，就是出现了乱码
        for (int i = 0; i < count; i++) {
            if (!matcher.find()) {
                log.info("该字符串乱码：{}", str);
                return true;
            }
        }
        return false;
    }
    
    /**
     * 类似java.lang.String的join方法，但可以对 不是CharSequence的子类的集合进行拼接
     * <p>模仿了jdk源码的写法
     */
    public static String join(CharSequence delimiter, Collection<?> elements) {
        Objects.requireNonNull(delimiter);
        Objects.requireNonNull(elements);
        
        StringJoiner joiner = new StringJoiner(delimiter);
        elements.forEach(element -> {
            joiner.add(element.toString());
        });
        return joiner.toString();
    }
    
    /**
     * 将给定字符串转化为小写下划线式写法（如：lower_underscore）。
     * 
     * @param str
     * @return
     */
    public static String toLowerUnderscore(String str) {
        // 若给定字符串已包含下划线，则转化为小写后直接返回。
        if (str.contains("_")) {
            return str.toLowerCase();
        }
        // 若给定字符串第一个字符为大写，则转化为小写后直接返回。
        if (str.length() > 0 && Ascii.isUpperCase(str.charAt(0))) {
            return str.toLowerCase();
        }
        // 将给定字符串视为小写驼峰式写法，利用guava的工具类进行转换
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, str);
    }
    
    /**
     * 由于util包没有Spring环境，不能用Spring的StringUtils的isEmpty方法，只能自己造一个轮子
     * 
     * @param obj
     * @return
     */
    public static boolean isEmpty(Object obj) {
        return obj == null || obj.equals("");
    }
    
    private static final String[] EXP_SP = 
        { "\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|" };
    
    /**
     * 转义正则特殊字符 （$()*+.[]?\^{},|）
     * 
     * <p>https://blog.csdn.net/bbirdsky/article/details/45368709
     * 
     * @param keyword
     * @return
     */
    public static String escapeExprSpecialWord(String keyword) {
        if (!StringUtils.isEmpty(keyword)) {
            for (String key : EXP_SP) {
                if (keyword.contains(key)) {
                    keyword = keyword.replace(key, "\\" + key);
                }
            }
        }
        return keyword;
    }


}
