package indi.core.util;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

class StringUtilsTest {

//    @Test
    void test() {
        boolean result = StringUtils.isChaos("[無組] ワスレナイキミ (コ1号).rar");
        System.out.println(result);
        boolean result2 = StringUtils.isChaos("う");
        System.out.println(result2);
        boolean result3 = StringUtils.isChaos("123.zip[【]\\");
        System.out.println(result3);
    }
    
    @Test
    void joinTest() {
        ArrayList<String> newArrayList = Lists.newArrayList();
        String result = StringUtils.join(".", newArrayList);
        System.out.println(result);
    }

}
