package indi.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TestSeparateExtension.class)
class CompressionUtilsTest {

    @Test
    void test() {
        CompressionUtils.packZip2Dir("E:\\downloadCache\\sts-4.0.1.RELEASE\\readme", 
                "E:\\downloadCache\\sts-4.0.1.RELEASE\\readme", true);
    }

}
