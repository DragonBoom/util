package indi.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import indi.exception.WrapperException;

class ExceptionUtilsTest {

    @Test
    void test() {
        try {
            throw new IllegalArgumentException("非法参数");
        } catch (Throwable e) {
            try {                
                throw new WrapperException(e);
            } catch (Exception e2) {
                try {
                    throw new RuntimeException(e2);
                } catch (Exception e3) {
                    WrapperException e4 = new WrapperException(e3);
                    Throwable firstCause = ExceptionUtils.findFirstCause(e4);
                    Assertions.assertEquals(e, firstCause);
                }
            }
        }
        
    }

}
