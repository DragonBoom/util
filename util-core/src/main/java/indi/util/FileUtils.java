package indi.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileUtils {

    /**
     * 若目录不存在，则创建目录
     * 
     * @param pathStr
     * @return 若创建了文件夹则返回true
     */
    public static final boolean createDirectoryIfNotExist(String pathStr) {
        Path path = Paths.get(pathStr);
        if (!Files.isDirectory(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return true;
        }
        return false;
    }

    /**
     * 清空目录（删除目录所有内容，包括目录本身）
     * 
     * @param directory
     */
    public static final void clearDirectory(Path directory) {
        try {
            Files.walkFileTree(directory, new DeleteFileVisitor());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class DeleteFileVisitor extends SimpleFileVisitor<Path> {

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            log.info("删除文件夹: {}", dir);
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            log.info("删除文件: {}", file);
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }
    }
}
