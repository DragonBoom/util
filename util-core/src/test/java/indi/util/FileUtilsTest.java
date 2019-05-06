package indi.util;

import java.io.IOException;
import java.nio.file.Paths;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TestSeparateExtension.class)
class FileUtilsTest {

    @Test
    @Disabled
    void createTest() throws IOException {
        int count = FileUtils.createDirectoryIfNotExist(Paths.get("e:", "test", "test", "test"));
        System.out.println(count);
    }
    
    @Test
    void moveTest() {
        FileUtils.move(Paths.get("d:", "test"), Paths.get("e:", "/"));
    }

}
