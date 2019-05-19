package indi.util;

import java.io.IOException;
import java.nio.file.Path;
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
    @Disabled
    void moveTest() {
        FileUtils.move(Paths.get("d:", "test"), Paths.get("e:", "/"));
    }

    @Test
    void findDirTest() {
        Path directory = FileUtils.findDirectory(Paths.get("e:", "/"), "hauxsoft");
        System.out.println(directory);
    }
}
