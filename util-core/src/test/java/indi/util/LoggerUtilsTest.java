package indi.util;

import org.junit.jupiter.api.Test;

import indi.util.LoggerUtils;

class LoggerUtilsTest {

    @Test
    void test() {
        LoggerUtils.getLogger().info("test");
    }

}
