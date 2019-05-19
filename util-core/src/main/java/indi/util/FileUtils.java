package indi.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Consumer;

import indi.exception.WrapperException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileUtils {

    /**
     * 若目录不存在，则创建目录。若目录的上级目录也不存在，将一并创建。
     * 
     * @return 返回创建的目录数
     */
    public static final int createDirectoryIfNotExist(String pathStr) {
        Path path = Paths.get(pathStr);
        return createDirectoryIfNotExist(path);
    }

    /**
     * 若目录不存在，则创建目录。若目录的上级目录也不存在，将一并创建。
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
        } else {
            // 校验给定路径是否指向文件
            if (!Files.isDirectory(path)) {
                throw new IllegalArgumentException(path + " 指向文件");
            }
        }
        return count;
    }

    /**
     * 清空目录（删除目录所有内容，包括目录本身）。若给定目录不存在或不是目录，将直接返回。
     * 
     * @param directory
     */
    public static final void clearDirectory(Path directory) {
        if (!validDirectory(directory, false)) {
            return;
        }
        
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
     * 
     * @exception IllegalArgumentException 源地址不存在或不是指向目录
     */
    public static final void move(Path source, Path dir) {
        validDirectory(source, true);

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

    @AllArgsConstructor
    private static class MoveFileVisitor extends SimpleFileVisitor<Path> {
        private Path source;
        private Path dest;
        
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
    
    /**
     * 批量处理给定目录下的所有目录（含给定目录本身）
     * 
     * @exception IllegalArgumentException 源地址不存在或不是指向目录
     */
    public static void forEachDirectory(Path dir, Consumer<Path> function) {
        validDirectory(dir, true);
        try {
            Files.walkFileTree(dir, new FunctionFileVisitor(null, function));
        } catch (IOException e) {
            throw new WrapperException(e);
        }
    }
    
    @AllArgsConstructor
    private static final class FunctionFileVisitor extends SimpleFileVisitor<Path> {
        private Consumer<Path> fileFun;
        private Consumer<Path> dirFun;

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
            Optional.ofNullable(dirFun).ifPresent(fun -> fun.accept(dir));
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Optional.ofNullable(fileFun).ifPresent(fun -> fun.accept(file));
            return FileVisitResult.CONTINUE;
        }
    }
    
    /**
     * 检测给定路径是否指向有效的文件夹，若不是返回false或抛异常
     * 
     * @param path
     * @param throwError 检测不通过时是否抛异常
     * @return
     */
    private static boolean validDirectory(Path path, boolean throwError) {
        if (!(Files.exists(path))) {
            if (throwError) {
                throw new IllegalArgumentException("给定路径不存在" + path.toString());
            }
            return false;
        }
        if (!Files.isDirectory(path)) {
            if (throwError) {
                throw new IllegalArgumentException("给定路径不是有效文件夹 " + path.toString());
            }
            return false;
        }
        return true;
    }
    
    /**
     * 从给定文件夹开始，查找文件夹。将基于宽度/广度优先(BFS Breadth-First-Search)进行搜索。
     * 
     * @param path 起始路径
     * @param directoryName 所查找的文件夹名
     */
    @SuppressWarnings("unchecked")
    public static final Path findDirectory(Path path, String directoryName) {
        validDirectory(path, true);
        LinkedList<Path> list = new LinkedList<>();
        LinkedList<Path> nextList = new LinkedList<>();
        list.add(path);
        while (list.size() > 0) {
            try {
                Iterator<Path> iterator = list.iterator();
                while (iterator.hasNext()) {
                    Path parentPath = iterator.next();
                    
                    for (Path subPath : Files.newDirectoryStream(parentPath, Files::isDirectory)) {
                        if (subPath.getFileName().toString().equals(directoryName)) {
                            return subPath;
                        }
                        nextList.add(subPath);
                    }
                    iterator.remove();
                }
                list = (LinkedList<Path>) nextList.clone();
                nextList.clear();
            } catch (IOException e) {
                throw new WrapperException(e);
            }
        }
        // till not found...
        return null;
    }
}
