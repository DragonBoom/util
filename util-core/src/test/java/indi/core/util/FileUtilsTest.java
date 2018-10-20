package indi.core.util;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class FileUtilsTest {

    @Test
    void test() {
        Path path = Paths.get("E:", "test");
        FileUtils.clearDirectory(path);
    }

}
