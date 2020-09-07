package indi.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import indi.io.FileUtils;
import indi.test.TestSeparateExtension;

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
        FileUtils.move2Dir(Paths.get("d:", "test"), Paths.get("e:", "/"));
    }

    @Test
    @Disabled
    void findDirTest() {
        Path directory = FileUtils.findDirectory(Paths.get("e:", "/"), "hauxsoft");
        System.out.println(directory);
    }
    
    @Test
    @Disabled
    void createTmpTest() {
        File tmpFile = FileUtils.createTmpFile("f://tmp");// print: f:\tmp\FileUtils-8731828804996020581.tmp
        System.out.println(tmpFile);
    }
    
    @Test
    @Disabled
    void getFileNameTest() {
        Path p = Paths.get("f:", "d", "c");
        System.out.println(p);
        String fileName = FileUtils.getFileName(p);
        System.out.println(fileName);
    }
    
    @Test
    void isEmptyTset() throws IOException {
        
        Path p = Paths.get("e:", "for test");
        System.out.println(Files.walk(p).count());// 空目录为1
        System.out.println(Files.walk(p).filter(p1 -> !Files.isDirectory(p1)).findFirst().isPresent());
    }
}
