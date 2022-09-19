package indi.util;

import java.lang.Character.UnicodeBlock;
import java.util.Collection;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Ascii;
import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableMap;

import lombok.extern.slf4j.Slf4j;

/**
 * ASCII码一览表: http://c.biancheng.net/c/ascii/
 * 
 * @author wzh
 * @since 2021.12.23
 */
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

    public static boolean haveChinese(String str) {
        for (Character c : str.toCharArray()) {
            if (!isChinese(c)) {
                return false;
            }
        }
        return true;
    }

    public static final boolean isEnglish(Character c) {
//        Range<Character> r1 = Range.closed('a', 'z');
//        Range<Character> r2 = Range.closed('A', 'Z');
//        return r1.contains(c) || r2.contains(c);
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
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
     * 转半角的函数（似乎网上流传最广的半角=DBC、全角=SBC是错的，实际上半角=SBC、全角=DBC）（这里折衷换个说法）
     * <p>
     * 全角空格为12288，半角空格为32； <br>
     * 其他字符半角(33-126)与全角(65281-65374)的对应关系是：均相差65248
     * <p>
     * 注意：也会转义常用的“”（），。等符号
     * 
     * @param input 任意字符串
     * @return 半角字符串
     * @see https://blog.csdn.net/lgywsdy/article/details/84561437
     */
    public static String toHalfSymbols(String input) {
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 12288) {
                // 全角空格为12288，半角空格为32
                c[i] = (char) 32;
            } else if (c[i] > 65280 && c[i] < 65375) {
                // 其他字符半角(33-126)与全角(65281-65374)的对应关系是：均相差65248
                c[i] = (char) (c[i] - 65248);
            }
        }
        return new String(c);
    }

    /**
     * 转全角的方法（似乎网上流传最广的半角=DBC、全角=SBC是错的，实际上半角=SBC、全角=DBC）（这里折衷换个说法）
     * <p>
     * 全角空格为12288，半角空格为32 其他字符半角(33-126)与全角(65281-65374)的对应关系是：均相差65248
     * <p>
     * 注意：也会转义常用的“”（），。等符号
     * @param input 任意字符串
     * @return 半角字符串
     * @see https://blog.csdn.net/lgywsdy/article/details/84561437
     */
    public static String toFullSymbols(String input) {
        // 半角转全角：
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 32) {
                c[i] = (char) 12288;
            } else if (c[i] < 127) {
                c[i] = (char) (c[i] + 65248);
            }
        }
        return new String(c);
    }
    
    
    /** 
     * windows文件名的<违法字符、违法字符全角>映射。用于对包含违法字符的文件名做转义或反转义<br>
     * 共有9个违法字符：\ / : * ? " < > |
     */
    public static final ImmutableMap<Character, Character> WIN_FILENAME_ILLEGAL_CHAR_REPLACE_MAP = ImmutableMap
            .<Character, Character>builder().put('\\', '＼').put('/', '／').put(':', '：').put('*', '＊').put('?', '？')
            .put('"', '＂').put('<', '＜').put('>', '＞').put('|', '｜').build();
    
    /**
     * 类似java.lang.String的join方法，但可以对 不是CharSequence的子类的集合进行拼接
     * <p>
     * 模仿了jdk源码的写法
     */
    public static String join(CharSequence delimiter, Collection<?> elements) {
        Objects.requireNonNull(delimiter);
        Objects.requireNonNull(elements);

        StringJoiner joiner = new StringJoiner(delimiter);
        elements.forEach(element -> joiner.add(element.toString()));
        return joiner.toString();
    }

    public static String join(CharSequence delimiter, String[] elements) {
        Objects.requireNonNull(delimiter);
        Objects.requireNonNull(elements);

        StringJoiner joiner = new StringJoiner(delimiter);
        for (String ele : elements) {
            joiner.add(ele);
        }
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
        // 若给定字符串第一个字符为大写，则转化为小写后直接返回。FIXME:
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

    public static boolean notEmpty(Object obj) {
        return !isEmpty(obj);
    }

    private static final String[] EXP_SP = { "\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|" };

    /**
     * 转义正则特殊字符 （$()*+.[]?\^{},|）
     * 
     * <p>
     * https://blog.csdn.net/bbirdsky/article/details/45368709
     * 
     * @param keyword
     * @return
     */
    public static String escapeExpSpecialWord(String keyword) {
        if (!StringUtils.isEmpty(keyword)) {
            for (String key : EXP_SP) {
                if (keyword.contains(key)) {
                    keyword = keyword.replace(key, "\\" + key);
                }
            }
        }
        return keyword;
    }

    /**
     * 移除无用空格。本方法会保持尽量可能地通用。
     * 
     * @param name
     * @param needLog
     * @return
     * @since 2021.01.25
     */
    public static String removeUselessSpace(String name) {
        if (name.length() < 2) {
            return name;
        }
        String newName = name;

        // 去掉首尾空格
        newName = newName.trim();

        // 遍历标记-遍历处理
        // a. 移除括号内紧邻的空格，如[ a或a ]中的空格；
        // b. 将连续空格替换为1个空格
        char[] chars = newName.toCharArray();
        // 遍历标记
        int[] marks = new int[chars.length];
        for (int i = 0; i < chars.length; i++) {
            // 移除左括号右边的空格
            if (i + 1 < chars.length && chars[i + 1] == ' ' && "[(【（".indexOf(chars[i]) != -1) {
                marks[i + 1] = -1;// 标记清除
            }
            // 移除右括号左边的空格 || 移除连续空格（原本采用正则替换实现）
            else if (i - 1 > 0 && chars[i - 1] == ' ' && ("])】）".indexOf(chars[i]) != -1 || chars[i] == ' ')) {
                marks[i - 1] = -1;// 标记清除
            }
        }
        // 遍历处理
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < marks.length; i++) {
            if (marks[i] != -1) {
                sb.append(chars[i]);
            }
        }
        newName = sb.toString();
        // others...
        return newName;
    }

    /**
     * 检查是否有嵌套同类括号，若有则返回true，否则返回false
     * 
     * @param folderName
     * @param left       左括号
     * @param right      右括号
     * @since 2021.03.13
     */
    public static boolean checkNestedBrackets(String folderName, char left, char right) {
        boolean preLeft = false;// 上一个括号是否为左括号
        boolean preRight = false;
        for (char c : folderName.toCharArray()) {
            if (c == left) {
                if (preLeft) {
                    return true;
                }
                preLeft = true;
                preRight = false;
            } else if (c == right) {
                if (preRight) {
                    return true;
                }
                preLeft = false;
                preRight = true;
            }
        }
        return false;
    }

}
