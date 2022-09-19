package indi.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import indi.io.FileUtils;
import indi.io.FileUtils.FileNameComparator;
import indi.test.TestSeparateExtension;

@ExtendWith(TestSeparateExtension.class)
class FileUtilsTest {

    @Test
    @Disabled
    void createTest() throws IOException {
        Path path = Paths.get("e:", "test");
        FileUtils.createEmptyFileIfNotExist(Paths.get("e:", "test"));
        try (BufferedReader br = Files.newBufferedReader(path)) {
            System.out.println(br.readLine());
        }
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
        Path p = Paths.get("f:", "dff", "c.jpg");
        System.out.println(p);
        String fileName = FileUtils.getFileName(p);
        System.out.println(fileName);
        Assertions.assertEquals("c", fileName);
    }
    
    @Test
    @Disabled
    void isEmptyTset() throws IOException {
        
        Path p = Paths.get("e:", "for test");
        System.out.println(Files.walk(p).count());// 空目录为1
        System.out.println(Files.walk(p).filter(p1 -> !Files.isDirectory(p1)).findFirst().isPresent());
    }
    
    @Test
    @Disabled
    void fileNameComparatorTest() {
        FileNameComparator comparator = new FileUtils.FileNameComparator();
        Assertions.assertEquals(-1, comparator.compare("a001.jpg", "b002.jpg"));
        Assertions.assertEquals(-1, comparator.compare("01.jpg", "012.jpg"));
        Assertions.assertEquals(1, comparator.compare("015b.jpg", "015a.jpg"));
    }
    
    @Test
    @Disabled
    void sizeTest() {
        Assertions.assertTrue(FileUtils.dirSize(Paths.get("f:", "byCrawler", "konachan"), false) > 0);
    }
    
    @Test
    @Disabled
    void readAllLinesForBOMTest() throws IOException {
        Path path = Paths.get("e:", "utf8bom.txt");
        List<String> lines = FileUtils.readAllLinesForBOM(path, StandardOpenOption.READ);
        Assertions.assertNotNull(lines);
    }
    
    @Test
    @Disabled
    void moveOrRename() {
        Path source = Paths.get("e:", "test.txt");
        Path dest = Paths.get("e:", "TeSt.txt");
        FileUtils.moveOrRename(source, dest);
    }
    
}
