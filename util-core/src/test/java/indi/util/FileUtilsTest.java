package indi.util;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import indi.util.FileUtils;

class FileUtilsTest {

    @Test
    void test() {
        Path path = Paths.get("E:", "test");
        FileUtils.clearDirectory(path);
    }

}
