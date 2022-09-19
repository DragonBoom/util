/**
 * 
 */
package indi.util;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import indi.data.Three;

/**
 * @author wzh
 * @since 2020.09.18
 */
class ProcessUtilsTest {

    @Test
//    @Disabled
    void testProcess() throws IOException {
        // 将作者名称写入到comment字段（xnview可用该字段排序）
        String[] commands = new String[] { "exiftool"};
        Three<String,String,Integer> process = ProcessUtils.process(commands, null);
        System.out.println(process);
        System.out.println(123);
    }

}
