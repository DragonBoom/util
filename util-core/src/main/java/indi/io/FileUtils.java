package indi.io;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSet;

import indi.exception.WrapperException;
import indi.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileUtils {
    
    /**
     * 清空文件
     * 
     * @param path
     * @return
     */
    public static final Boolean emptyFile(Path path) {
        
        Boolean isCreatedNew = createEmptyFileIfNotExist(path);
        if (isCreatedNew) {
            // 已创建新的空文件
            return true;
        }
        
        try {
            long size = Files.size(path);
            if (size == 0) {
                // 本来就是空文件
                return false;
            }
            
            // 用空字符串覆盖文件内容
            Files.write(path, new String().getBytes(), 
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            throw new WrapperException(e);
        }
    }
    
    /**
     * 与 Files.createFile(Path) 相比，多了是否文件夹的校验，且当文件已存在时不会抛异常
     * 
     * @param path
     * @return true 表示有创建文件，false表示没创建文件（文件已存在）
     * @throws IOException
     */
    public static final Boolean createEmptyFileIfNotExist(Path path) {
        if (Files.isDirectory(path)) {
            throw new IllegalArgumentException("路径 [" + path + "] 指向的是目录而不是文件！");
        }
        boolean exists = Files.exists(path);
        if (exists) {
            return false;
        }
        try {
            Files.createFile(path);
            return true;
        } catch (FileAlreadyExistsException e) {
            // do nothing
            return false;
        } catch (IOException e) {
            throw new WrapperException(e);
        }
    }

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
        // 利用栈的先进后出实现
        LinkedList<Path> stack = new LinkedList<>();
        Path parent = null;

        while (!Files.exists(path) && (parent = path.getParent()) != null) {
            stack.offerFirst(path);
            path = parent;
        }
        
        while (!stack.isEmpty()) {
            try {
                Files.createDirectories(stack.pollFirst());
            } catch (IOException e) {
                throw new WrapperException(e);
            }
        }
        return count;
    }
    
    /** 不抛异常的Files.delete(Path) */
    public static final void delete(Path p) {
        try {
            Files.delete(p);
        } catch (IOException e) {
            throw new WrapperException(e);
        }
    }

    /** 不抛异常的Files.list(Path)，务必主动关闭流 */
    public static Stream<Path> list(Path p) {
        try {
            return Files.list(p);
        } catch (IOException e) {
            throw new WrapperException(e);
        }
    };

    /**
     * 清空目录。若给定目录不存在或不是目录，将直接返回。
     * 
     * @param directory
     * @param deleteSelf 是否删除目录本身
     */
    public static final void clearDirectory(Path directory, boolean deleteSelf) {
        if (!validDirectory(directory, false)) {
            return;
        }
        
        try {
            Files.walkFileTree(directory, new DeleteFileVisitor(directory, deleteSelf));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @AllArgsConstructor
    private static class DeleteFileVisitor extends SimpleFileVisitor<Path> {
        private Path source;
        private boolean deleteSource;
        
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            if (deleteSource || !source.equals(dir)) {
                log.debug("删除文件夹: {}", dir);
                Files.delete(dir);
            }
            
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
     * 将文件移动到指定路径，若目标文件存在，将覆盖
     */
    public static final Path moveFile2Dir(Path file, Path dir) {
        Path dest = dir.resolve(file.getFileName());
        try {
            Files.move(file, dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new WrapperException(e);
        }
        return dest;
    }

    /**
     * 移动文件/文件夹到指定路径下，若涉及到多个文件（夹），将逐个处理。将把源目录整个移动到目标路径<b>下</b>
     * 
     * <p>因为是将源目录移动到指定目录下，因此该方法无法用于重命名
     * 
     * <p>本方法其实完全可以用Files.move替代，只是省去了计算目标路径；并且，移动目录时，会逐个执行Files.move，而不是只操作整个目录
     * 
     * @param sourceDir 待移动目录
     * @param dir 目标目录
     * @exception IllegalArgumentException 源地址不存在或不是指向目录
     */
    public static final void move2Dir(Path sourceDir, Path dir) {
        validDirectory(sourceDir, true);

        FileUtils.createDirectoryIfNotExist(dir);

        Path dest = dir.resolve(sourceDir.getFileName());
        FileUtils.createDirectoryIfNotExist(dest);

        if (Files.isDirectory(sourceDir)) {
            MoveFileVisitor fileVisitor = new MoveFileVisitor(sourceDir, dest);
            try {
                Files.walkFileTree(sourceDir, fileVisitor);
            } catch (IOException e) {
                throw new WrapperException(e);
            }
        } else {
            try {
                Files.move(sourceDir, dest, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new WrapperException(e);
            }
        }
    }
    
    /**
     * 逐个文件地移动目录，可用于重命名
     * 
     * @author DragonBoom
     * @since 2020.08.31
     * @param sourceDir 源目录
     * @param dest 目标目录，必须已存在
     */
    public static final void moveDirEach(Path sourceDir, Path dest) {
        validDirectory(sourceDir, true);
        validDirectory(dest, true);
        if (sourceDir.equals(dest)) {
            throw new IllegalArgumentException("源路径与目标路径相同，无法移动");
        }

        MoveFileVisitor fileVisitor = new MoveFileVisitor(sourceDir, dest);
        try {
            Files.walkFileTree(sourceDir, fileVisitor);
        } catch (IOException e) {
            throw new WrapperException(e);
        }
    }

    /**
     * 移动访问器，用于逐个移动目录下的文件
     * 
     * @author wzh
     */
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
            log.debug("退出并删除文件夹: {}", dir);
            // 此时该目录理论上是空目录
            /*
             * 之前删除目录的行为可能无法立即生效，从而导致无法删除嵌套目录（抛目录不为空的异常）
             */
            try {
                Files.delete(dir);
            } catch (DirectoryNotEmptyException e) {
                // 此时打断点，可以用管理器看到目录下确实仍有其他目录，且这些目录无法访问：位置不可用
                log.error("can`t delete empty directory: {} !!!", dir);
            } catch (AccessDeniedException e) {
                log.error("can`t access directory: {} !!!", dir);
            }
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
                    
                    try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(parentPath, Files::isDirectory)) {
                        for (Path subPath : dirStream) {
                            if (subPath.getFileName().toString().equals(directoryName)) {
                                return subPath;
                            }
                            nextList.add(subPath);
                        }
                        
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
    
    /**
     * Files.readAllBytes的不抛异常版本
     */
    public static final byte[] readAllBytes(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new WrapperException(e);
        }
    }
    
    private static final String TMP_PREFIX = "FileUtils-";// len must > 3 
    
    /**
     * 在系统指定目录下生成临时文件。对返回的File对象，可调用其toPath方法转化为Path对象（不提供仅将File转化为Path的轮子，避免使接口复杂化）
     * 
     * @param tmpDirPathStr 生成临时文件的目录，可为空，为空时取系统默认的临时文件夹。建议与临时文件的最终存储路径位于同一个磁盘，从而提高移动的效率
     * @return
     */
    public static File createTmpFile(String tmpDirPathStr) {
        if (!StringUtils.isEmpty(tmpDirPathStr)) {
            FileUtils.createDirectoryIfNotExist(Paths.get(tmpDirPathStr));
        }
        // 利用Java自带的api实现
        try {
            return File.createTempFile(TMP_PREFIX, null, new File(tmpDirPathStr));
        } catch (IOException e) {
            throw new WrapperException(e);
        }
    }
    
    /**
     * 获取文件的后缀
     * 
     * @param fileName 文件路径或文件名
     * @return 后缀字符串。文件没有后缀时返回null。
     */
    public static String getSuffix(String fileName) {
        int i = fileName.lastIndexOf('.');
        return i == -1 ? null : fileName.substring(i + 1);
    }
    
    /**
     * 获取文件名（不含后缀）
     * 
     * @param path 文件路径或文件名
     * @return 文件名
     */
    public static String getFileName(String path) {
        // 基于正则实现
        Matcher matcher = Pattern.compile("(?<=(\\\\|/|^))[^(\\\\|/)]+?(?=(\\.[^(\\\\|/)]{1,9}|$))").matcher(path);
        return matcher.find() ? matcher.group() : null;
    }
    
    /**
     * 获取文件名（不含后缀）
     * 
     * @param path 文件路径或文件名
     * @return 文件名
     */
    public static String getFileName(Path path) {
        return getFileName(path.toString());
    }
    
    /**
     * 图片文件后缀，小写，第一版取自ImageIO.getReaderFileSuffixes()
     */
    public static final ImmutableSet<String> imageSuffixes = ImmutableSet.of("jpg", "bmp", "gif", "png", "jpeg", "wbmp");
    
    /**
     * 判断文件是否为图片。只通过后缀是否为"jpg", "bmp", "gif", "png", "jpeg", "wbmp"进行判断（不区分大小写），
     * 不会实际解析文件内容去判断
     * 
     * @return
     */
    public static Boolean isImage(Path path) {
        return Optional.ofNullable(getSuffix(path.toString()))
                .map(suffix -> imageSuffixes.contains(suffix.toLowerCase()))
                .orElse(false);
    }
    
    private static final Integer DEFAULT_ALLOCATE_BUFFER_SIZE = 2048;
    
    /**
     * 复制Channel的内容，将不会关闭任何通道！将从source的起始位置开始复制(position)，复制到target的其实位置(position)。
     * 
     * 该方法用于需要手动操作流复制的场景，可根据需要进行修改（以增加参数为主）
     * 
     * @param source source
     * @param target target
     * @param bufferSize 复制时所使用的缓存的大小，可为null，null时取默认值
     */
    public static void copyChannel(ReadableByteChannel source, WritableByteChannel target, Integer bufferSize) {
        if (bufferSize == null) {
            bufferSize = DEFAULT_ALLOCATE_BUFFER_SIZE;
        }
        ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
        // 校验：
        // a. 是否已关闭
        if (!source.isOpen()) {
            throw new IllegalArgumentException(source + " is closed!");
        }
        if (!target.isOpen()) {
            throw new IllegalArgumentException(target + " is closed!");
        }
        // 复制字节
        try {
            while (source.read(buffer) != -1) {
                buffer.flip();
                target.write(buffer);
                buffer.clear();
            }
        } catch (IOException e) {
            throw new WrapperException(e);
        }
    }
    
    /**
     * 校验目标目录是否为空；若路径不存在或不是指向目录，也不会抛异常，只返回false
     * 
     * @author DragonBoom
     * @since 2020.08.31
     * @param dir
     * @param allowEmptyDir 是否允许有空的子目录
     * @return
     */
    public static boolean isEmptyDir(Path dir, boolean allowEmptyDir) {
        if (!validDirectory(dir, false)) {
            return false;
        }
        
        try (Stream<Path> fStream = Files.walk(dir)) {
            if (allowEmptyDir) {
                // 递归检查是否没有任何文件
                return !fStream.filter(p -> !Files.isDirectory(p)).findFirst().isPresent();
            } else {
                // 递归检查是否没有条目
                return !fStream.filter(p -> !p.equals(dir)).findFirst().isPresent(); 
            }
        } catch (IOException e) {
            throw new WrapperException(e);
        }
    }
    
    /**
     * 检验给定路径是否指向文件（存在且不是目录）
     * 
     * @param path
     * @return
     * @author DragonBoom
     * @since 2020.09.05
     */
    public static boolean isFile(Path path) {
        return Files.exists(path) && !Files.isDirectory(path);
    }
}
