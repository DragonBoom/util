package indi.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

import indi.exception.WrapperException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileUtils {

    /**
     * 若目录不存在，则创建目录
     * 
     * @return 返回创建的目录数
     */
    public static final int createDirectoryIfNotExist(String pathStr) {
        Path path = Paths.get(pathStr);
        return createDirectoryIfNotExist(path);
    }

    /**
     * 若目录不存在，则创建目录
     * 
     * @return 返回创建的目录数
     */
    public static final int createDirectoryIfNotExist(Path path) {
        int count = 0;
        if (!Files.exists(path)) {
            Path parent = path.getParent();
            if (parent == null) {
                return 0;
            }
            if (!Files.exists(parent)) {
                count += createDirectoryIfNotExist(parent);
            }
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            count++;
        }
        return count;
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

    private static class DeleteFileVisitor extends SimpleFileVisitor<Path> {

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            log.debug("删除文件夹: {}", dir);
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            log.debug("删除文件: {}", file);
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }
    }

    /**
     * 移动文件（夹），若涉及到多个文件（夹），将逐个处理
     */
    public static final void move2Directory(Path source, Path dir) {
        if (!Files.isDirectory(dir)) {
            throw new IllegalArgumentException(dir + "不是文件夹");
        }

        FileUtils.createDirectoryIfNotExist(dir);

        Path dest = dir.resolve(source.getFileName());
        FileUtils.createDirectoryIfNotExist(dest);

        if (Files.isDirectory(source)) {
            MoveFileVisitor fileVisitor = new MoveFileVisitor(source, dest);
            try {
                Files.walkFileTree(source, fileVisitor);
            } catch (IOException e) {
                throw new WrapperException(e);
            }
        } else {
            try {
                Files.move(source, dest, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new WrapperException(e);
            }
        }
    }

    private static class MoveFileVisitor extends SimpleFileVisitor<Path> {
        private Path source;
        private Path dest;
        
        public MoveFileVisitor(Path source, Path dest) {
            super();
            this.source = source;
            this.dest = dest;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            log.debug("进入文件夹 {}", dir);
            
            Path dest2 = dest.resolve(source.relativize(dir));
            if (!Files.exists(dest2)) {
                log.debug("创建文件夹 {}", dest2);
                FileUtils.createDirectoryIfNotExist(dest2);
            }

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            log.debug("删除文件夹: {}", dir);
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Path dest2 = dest.resolve(source.relativize(file));
            log.debug("移动文件: {} -> {}", file, dest2);
            Files.move(file, dest2, StandardCopyOption.REPLACE_EXISTING);
            return FileVisitResult.CONTINUE;
        }

    }
}
