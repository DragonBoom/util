package indi.util;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import indi.test.TestSeparateExtension;
import indi.util.CompressionUtils.ResultTypes;
import net.lingala.zip4j.util.Zip4jConstants;

@ExtendWith(TestSeparateExtension.class)
class CompressionUtilsTest {
    
    @Test
    void unpackRarTest() {
        ResultTypes result = CompressionUtils.unpackRar("e:\\test2.rar", "e:\\test\\", null);
        System.out.println(result);
    }

    @Test
    @Disabled
    void test() {
        CompressionUtils.packZip2Dir("E:\\downloadCache\\sts-4.0.1.RELEASE\\readme", 
                "E:\\downloadCache\\sts-4.0.1.RELEASE\\readme", true);
    }
    
    @Test
    @Disabled
    void comLevelTest() {
        CompressionUtils.packZip("E:\\downloadCache\\440.97-desktop-win10-64bit-international-whql.exe", "E:\\downloadCache\\test.zip", 
                null, Zip4jConstants.DEFLATE_LEVEL_ULTRA, true);
    }

}
