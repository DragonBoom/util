package indi.core.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class FileUtils {

    /**
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
            LoggerUtils.getLogger().info("删除文件夹: {}", dir);
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            LoggerUtils.getLogger().info("删除文件: {}", file);
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }
    }
}
