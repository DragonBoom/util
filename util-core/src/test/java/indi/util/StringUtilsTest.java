package indi.util;

import java.util.ArrayList;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

class StringUtilsTest {

    @Test
    @Disabled
    void test() {
        boolean result = StringUtils.isChaos("[無組] ワスレナイキミ (コ1号).rar");
        System.out.println(result);
        boolean result2 = StringUtils.isChaos("う");
        System.out.println(result2);
        boolean result3 = StringUtils.isChaos("123.zip[【]\\");
        System.out.println(result3);
    }
    
    @Test
    @Disabled
    void joinTest() {
        ArrayList<String> newArrayList = Lists.newArrayList();
        String result = StringUtils.join(".", newArrayList);
        System.out.println(result);
    }
    
    @Test
//    @Disabled
    void isEnTest() {
        Assertions.assertTrue(StringUtils.isEnglish('f'));
        Assertions.assertTrue(StringUtils.isEnglish('f'));
        Assertions.assertTrue(StringUtils.isEnglish('F'));
        Assertions.assertTrue(StringUtils.isEnglish('a'));
        Assertions.assertTrue(StringUtils.isEnglish('A'));
        Assertions.assertTrue(StringUtils.isEnglish('z'));
        Assertions.assertTrue(StringUtils.isEnglish('Z'));
    }
    
    @Test
//    @Disabled
    void removeUselessSpace() {
        String result = StringUtils.removeUselessSpace("a  f [ f ] ");
        Assertions.assertEquals("a f [f]", result);
    }
    
    @Test
    void checkNestedBrackets() {
        boolean result = StringUtils.checkNestedBrackets("[a(bc)c]", '[', ']');
        Assertions.assertFalse(result);
        Assertions.assertTrue(StringUtils.checkNestedBrackets("[a[bc]c]", '[', ']'));
    }
    
    @Test
    void toHalfSymbolsTest() {
        String source = "（符号）";
        String r = StringUtils.toHalfSymbols(source);
        System.out.println(r);
    }

}
