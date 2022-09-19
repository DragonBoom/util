/**
 * 
 */
package indi.util;

import java.nio.file.Paths;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import indi.test.TestSeparateExtension;

/**
 * @author wzh
 * @since 2021.01.08
 */
@ExtendWith(TestSeparateExtension.class)
class CheckUtilsTest {

    @Test
    @Disabled
    void compareTest() {
        // 处理60mb文件需900millis
        int result = CheckUtils.compare(Paths.get("e:", "file1"), Paths.get("e:", "file2"));
        Assertions.assertEquals(0, result);
        result = CheckUtils.compare(Paths.get("e:", "file1"), Paths.get("e:", "file3"));
        Assertions.assertNotEquals(0, result);
    }
    
    @Test
    void crc32Test() {
        String crc32 = CheckUtils.getCRC32(Paths.get("e:", "file1"));
        System.out.println(crc32);
    }

}
