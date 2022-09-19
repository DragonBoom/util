package indi.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableSet;

import indi.exception.WrapperException;
import indi.util.StringUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtils {
     
    /**
     * 清空文件
     * 
     * @param path
     * @return
     */
    public static final Boolean emptyFile(Path path) {
        if (createEmptyFileIfNotExist(path)) {
            return true;
        }
        
        try {
            long size = Files.size(path);
            if (size == 0) {
                return false;
            }
            
            // 用空字符串覆盖文件内容
            Files.write(path, "".getBytes(), 
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
    public static final boolean createEmptyFileIfNotExist(Path path) {
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
                count++;
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
    }
    
    /** !Files.isDirectory(p, options)；FileUtils::notDirectory */
    public static boolean notDirectory(Path p, LinkOption... options) {
        return !Files.isDirectory(p, options);
    }

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
            throw new WrapperException(e);
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
     * 类似于Files.move(Path, Path)，不同之处在于，该方法可实现只修改文件名中的大小写
     * 
     * @param source
     * @param dest
     * @return 是否发生了移动或重命名
     * @since 2021.12.08
     */
    public static final boolean moveOrRename(Path source, Path dest) {
        validExists(source, true);
        validExists(dest, true);
        String sourceName = source.getFileName().toString();
        String destName = dest.getFileName().toString();
        if (sourceName.equals(destName)) {
            return false;
        }
        if (Files.isDirectory(source) ^ Files.isDirectory(dest)) {
            throw new IllegalArgumentException("给定路径一个指向文件，一个指向目录，无法处理：" + 
                    source.toString() + "  " + dest.toString());
        }
        if (sourceName.equalsIgnoreCase(sourceName)) {
            // 只修改文件名中的大小写，无法直接用Files.move实现
            // 需要先将源路径重命名为其他名字，再还原并修改大小写
            Path parent = source.getParent();
            String newName = destName + "1";
            while (Files.exists(parent.resolve(newName))) {
                newName = newName + "1";
            }
            Path newSource = parent.resolve(newName);
            try {
                Files.move(source, newSource);
                Files.move(newSource, dest);
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
        return true;
    }
    
    /**
     * 将【单个文件】移动到指定路径，若目标文件存在，将覆盖
     * 
     * @param file 待移动文件
     * @param dir 目标路径
     */
    public static final Path moveFile2Dir(Path file, Path dir) {
        validFile(file, true);
        Path dest = dir.resolve(file.getFileName());
        try {
            Files.move(file, dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new WrapperException(e);
        }
        return dest;
    }

    /**
     * 移动文件/文件夹到指定路径下，若涉及到多个文件（夹），将逐个处理。将把源目录整个移动到目标路径<b>下</b>，
     * 如：将/a移动到/b，结果为/a/b
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
     * 逐个文件地移动目录；可达到重命名的效果
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
        boolean result = validExists(path, throwError);
        if (result && !Files.isDirectory(path)) {
            if (throwError) {
                throw new IllegalArgumentException("给定路径不是有效目录： " + path.toString());
            }
            result = false;
        }
        return result;
    }
    
    /***
     * 验证给定路径是否指向有效的文件
     * 
     * @param path
     * @param throwError
     * @return
     * @since 2021.12.08
     */
    private static boolean validFile(Path path, boolean throwError) {
        boolean result = validExists(path, throwError);
        if (result && Files.isDirectory(path)) {
            if (throwError) {
                throw new IllegalArgumentException("给定路径指向目录而不是文件：" + path.toString());
            }
            result = false;
        }
        return result;
    }
    
    private static boolean validExists(Path path, boolean throwError) {
        if (!Files.exists(path)) {
            if (throwError) {
                throw new IllegalArgumentException("给定路径不存在：" + path.toString());
            }
            return false;
        } else {
            return true;
        }
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
        while (list.isEmpty()) {
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
    
    public static File createTmpFile(String tmpDir) {
        return FileUtils.createTmpFile(Paths.get(tmpDir));
    }
    
    /**
     * 在系统指定目录下生成临时文件。对返回的File对象，可调用其toPath方法转化为Path对象（不提供仅将File转化为Path的轮子，避免使接口复杂化）
     * 
     * @param tmpDir 生成临时文件的目录，可为空，为空时取系统默认的临时文件夹。建议与临时文件的最终存储路径位于同一个磁盘，从而提高移动的效率
     * @return
     */
    public static File createTmpFile(Path tmpDir) {
        FileUtils.createDirectoryIfNotExist(tmpDir);
        // 利用Java自带的api实现
        try {
            return Files.createTempFile(tmpDir, TMP_PREFIX, null).toFile();
        } catch (IOException e) {
            throw new WrapperException(e);
        }
    }
    
    private static final Pattern FILE_EXTENSION_PATTERN = Pattern.compile("(?<=\\.)[^\\.]+$");
    
    /**
     * 获取文件的后缀，小写，如：jpg
     * 
     * @param fileName 文件路径或文件名
     * @return 后缀字符串。文件没有后缀时返回null。
     */
    public static String getExtension(String fileName) {
        Matcher matcher = FILE_EXTENSION_PATTERN.matcher(fileName);
        return matcher.find() ? matcher.group().toLowerCase() : null;
    }
    
    /**
     * 获取文件的后缀，小写，如：jpg
     * 
     * @param fileName 文件路径或文件名
     * @return 后缀字符串。文件没有后缀时返回null。
     */
    public static String getExtension(Path path) {
        return getExtension(path.getFileName().toString());
    }
    
    // java多个不包含的无限匹配
    // 这里用?设置了最小匹配，因此只能识别只有一个.的文件名
    private static final Pattern FILE_NAME_PATTERN = Pattern.compile("(?<=\\\\|/|^)[^\\\\/]+?(?=\\.[^\\.]{0,10}$|$)");
    
    /**
     * 获取文件名（不含后缀）
     * 
     * @param path 文件路径或文件名
     * @return 文件名；无法识别返回null
     */
    public static String getFileName(String path) {
        Matcher matcher = FILE_NAME_PATTERN.matcher(path);
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
        return Optional.ofNullable(getExtension(path.toString()))
                .map(suffix -> imageSuffixes.contains(suffix.toLowerCase()))
                .orElse(false);
    }
    
    @SneakyThrows
    @Deprecated
    public static boolean isContainImg(Path dir) {
        validDirectory(dir, true);
        try (Stream<Path> stream = Files.list(dir)) {
            return stream.anyMatch(FileUtils::isImage);
        }
    }
    
    private static final Integer DEFAULT_ALLOCATE_BUFFER_SIZE = 2048;
    
    /**
     * 复制Channel的内容，将不会关闭任何通道！将从source的起始位置开始复制(position)，复制到target的起始位置(position)。
     * 
     * <p>该方法用于需要手动操作流复制的场景，可根据需要修改该方法（以增加参数为主）
     * 
     * @param source source
     * @param target target
     * @param bufferSize 复制时所使用的缓存的大小，可为null，null时取默认值
     * @param speedProcessFun 操作<数据传输量，实际间隔>的函数，将从该函数结束后才开始统计下一次间隔的时间；可为空
     * @param speedProcessMinInterval 执行的操作数据传输量的函数的最低间隔，单位为毫秒；可为空
     */
    public static void copyChannel(ReadableByteChannel source, WritableByteChannel target, @Nullable Integer bufferSize,
            @Nullable BiConsumer<Long, Long> speedProcessFun, @Nullable Long speedProcessMinInterval) {
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
            long lastMillis = System.currentTimeMillis();
            long size = 0;
            while (source.read(buffer) != -1) {
                buffer.flip();
                target.write(buffer);
                int limit = buffer.limit();
                
                if (speedProcessFun != null) {
                    long cost = System.currentTimeMillis() - lastMillis;// 本次处理时间
                    size += limit;
                    if (cost >= speedProcessMinInterval) {
                        speedProcessFun.accept(size, cost);
                        lastMillis = System.currentTimeMillis();// 
                        size = 0;
                    }
                }
                
                buffer.clear();
            }
        } catch (IOException e) {
            throw new WrapperException(e);
        }
    }
    
    public static void copyChannel(ReadableByteChannel source, WritableByteChannel target, Integer bufferSize) {
        copyChannel(source, target, bufferSize, null, null);
    }
    
    /**
     * 校验目标目录是否为空；若路径不存在或不是指向目录，也不会抛异常，只返回false
     * 
     * @author DragonBoom
     * @since 2020.08.31
     * @param dir
     * @param allowEmptyChild 是否允许有空的子目录
     * @return
     */
    @SneakyThrows
    public static boolean isEmptyDir(Path dir, boolean allowEmptyChild) {
        if (!validDirectory(dir, false)) {
            return false;
        }
        
        try (Stream<Path> fStream = Files.walk(dir)) {
            if (allowEmptyChild) {
                // 递归检查是否没有任何文件
                return fStream.allMatch(p -> !Files.isDirectory(p));
            } else {
                // 递归检查是否没有条目
                return fStream.allMatch(p -> !p.equals(dir)); 
            }
        }
    }
    
    /** 
     * 判断文件是否为空；通过读取一次磁盘条目属性实现，比多次调用Files的API效率更高
     * 
     * @param file 若指向目录将抛IllegalArgumentException异常
     * @return
     * @since 2020.09.29
     */
    public static boolean isFileEmpty(Path file) {
        try {
            BasicFileAttributes attributes = Files.readAttributes(file, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            if (attributes.isDirectory()) {
                throw new IllegalArgumentException("该路径指向目录而不是文件：" + file);
            }
            return attributes.size() == 0;
        } catch (IOException e) {
            // 参考Files.exists(Path, LinkOption...)
            return true;
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
    
    /**
     * 校验数字字符串的长度是否超过整型最大值（Long.MAX_VALUE）的长度，以此粗略判断能否将数字字符串转化为整型（Long）
     * 
     * @param numberStrs 所有字符均为数字的字符串的数组
     * @since 2021.09.23
     */
    private static void checkNumberStrLen(String... numberStrs) {
        for (String numberStr : numberStrs) {
            if (numberStr.length() > Long.toString(Long.MAX_VALUE).length()) {
                throw new IllegalArgumentException("数字字符串过长：" + numberStr);
            }
        }
    }
    
    /**
     * - 模仿windows的排序逻辑：
     * <li>1. 跳过相同的非数字前缀
     * <li>2. 若发现非数字不同，直接用该字符比较（注意文件名后缀也会参与比较）
     * <li>3. 若存在数字，找出完整数字
     * <li>3.1. 若完整数字相同，跳过后继续循环
     * <li>3.2. 若完整数字不同，比较数字（数字过大时只会抛异常，无法比较）
     * <li>4. 若一字符串是另一字符串的前缀，则前缀字符串较小
     * 
     * <p>通过抛特定异常来传递无法解析文件名的信息；比较的返回值只有-1、0、1，因为即使能返回具体数值其意义也是不确定的
     * 
     * 
     * @author wzh
     * @since 2020.09.06
     */
    public static class FileNameComparator implements Comparator<String> {
        
        /**
         * {@inheritDoc}
         * 
         * @return 只返回-1、0、1，因为即使能返回具体数值其意义也是不确定的
         */
        @Override
        public int compare(final String name1, final String name2) {
            if (StringUtils.isEmpty(name1) || StringUtils.isEmpty(name2)) {
                log.warn("比较参数不能为空");
                throw new CantCompareException("比较参数不能为空");
            }
            // 这里不该直接用equals、startsWith比较，而是应利用之后的遍历判断情况，以避免重复遍历两字符串
            int lenDiffer = name1.length() - name2.length();
            int minLen = lenDiffer >= 0 ? name2.length() : name1.length();
            char[] chars1 = name1.toCharArray();
            char[] chars2 = name2.toCharArray();

            for (int i = 0; i < minLen; i++) {
                char c1 = name1.charAt(i);
                char c2 = name2.charAt(i);
                if (!isNumber(c1) || !isNumber(c2)) {
                    // 两个字符不全是数字
                    if (c1 == c2) {
                       // 1. 两个字符不全是数字，且相同；跳过该字符，继续遍历
                        continue;
                    } else {
                        // 2. 两个字符不全是数字，且不同；则这两个字符的顺序就是文件名顺序
                        return c1 > c2 ? 1 : -1;
                    }
                } else {
                    // 3. 两个字符均为数字，可能相同也可能不同，需要继续遍历以找出完整数字才能作比较
                    // i = 数字开始下标
                    // 2022.02.28：发现这里会有无限循环
                    String number1 = getSerialNumberBeginAt(chars1, i);
                    String number2 = getSerialNumberBeginAt(chars2, i);
                    if (number1.equals(number2)) {
                        // 3.1. 数字字符串相同，跳过
                        i += number1.length() - 1;// 使i指向连续数字最后一个字符]
                        continue;
                    } else {
                        // 3.2. 字符串不同，转化为数字后比较
                        // 2022.02.28 ：由于可能会存在000和00000的字符串，因此需考虑返回0（之前没有）
                        try {
                            long l1 = Long.parseLong(number1);
                            long l2 = Long.parseLong(number2);
                            return l1 == l2 ? 0 : l1 > l2 ? 1 : -1;
                        } catch (NumberFormatException e) {
                            // 2021.09.23 添加字符串长度的检验 2021.12.14 当发生异常才校验，以提高占大多数的无异常情况的效率
                            checkNumberStrLen(number1, number2);
                            throw e;
                        }
                    }
                }
            }
            if (lenDiffer == 0) {
                // 两字符相同
                return 0;
            } else {
                // 4. 一字符串是另一字符串的前缀，前缀字符串较小
                return lenDiffer > 0 ? 1 : -1;
            }
        }

        
        /** 判断字符是否为数字 */
        private boolean isNumber(char c) {
            switch(c) {
            case '0': case '1':case '2':case '3':case '4':case '5':case '6':case '7':case '8':case '9': return true;
            default: return false;
            }
        }
        
        /**
         * 从指定下标开始，获取连续数字字符串
         * 
         * @param str
         * @param beginIndex 必须小于chars.length，否则将返回空字符串
         * @return 不会抛异常或返回null，最多返回空字符串
         * @author DragonBoom
         * @since 2020.09.12
         */
        private String getSerialNumberBeginAt(char[] chars, int beginIndex) {
            if (chars == null || chars.length == 0) {
                return "";
            }
            // 只遍历数字，获取最后一个数字的下标
            StringBuilder sb = new StringBuilder();
            int i = beginIndex;
            while (i < chars.length) {
                char c = chars[i];
                if (isNumber(c)) {
                    sb.append(c);
                    i++;
                } else {
                    break;
                }
            }
            return sb.toString();
        }
        
        /** 抛出该异常表示无法比较 */
        public static class CantCompareException extends RuntimeException {
            private static final long serialVersionUID = 1L;
            private CantCompareException(String message) {
                super(message);
            }
        }
    }
    
    /**
     * 读取格式含bom的文件。BOM的作用为标记unicode的格式（判断是utf-16还是utf-8），无法用Java提供的API获取该类型的文件
     * 
     * <p>更多可见：https://stackoverflow.com/questions/4897876/reading-utf-8-bom-marker
     * 
     * @param path
     * @param options
     * @return 若文件不含bom位返回null
     * @author DragonBoom
     * @since 2020.09.13
     */
    @SneakyThrows
    public static List<String> readAllLinesForBOM(Path path, OpenOption... options) {
        LinkedList<String> result = new LinkedList<>();
        InputStream inputStream = Files.newInputStream(path, StandardOpenOption.READ);
        // 使用Apache的BOMInputStream处理
        try (BOMInputStream bOMInputStream = new BOMInputStream(inputStream)) {
            ByteOrderMark bom = bOMInputStream.getBOM();
            if (bom == null) {
                return null;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(bOMInputStream, bom.getCharsetName()));
            //use reader
            String line = null;
            while ((line = reader.readLine()) != null) {
                result.add(line);
            }
        }
        return result;
    }
    
    /**
     * 获取给定目录的大小。通过目录下的遍历所有文件实现
     * 
     * @param path
     * @param recursion 是否递归统计子目录的大小
     * @return 单位为字节
     * @author DragonBoom
     * @since 2020.09.16
     */
    @SneakyThrows
    public static long dirSize(Path path, boolean recursion) {
        validDirectory(path, true);
        
        try (Stream<Path> stream = recursion ? Files.walk(path) : Files.list(path)) {
            return stream
                    // 文件夹的size为0，且isDirectory与size都是通过读文件属性实现，该过滤并不能提高效率
                    // .filter(p -> !Files.isDirectory(path))
                    .map(file -> {
                        try {
                            return Files.size(file);
                        } catch (IOException e) {
                            throw new WrapperException(e);
                        }
                    })
                    .reduce(0l, (total, next) -> total + next);// 第一个参数是累加的起始值
        }
    }
    
    
    /**
     * 通过比较指定文件的属性，找出不是最大的路径；可用于找出不是最大或不是最新的文件
     * 
     * @param <T> 必须实现Comparable接口，即提供compareTo(T)方法
     * @param paths
     * @param getter 从文件属性中获取比较值
     * @param largerThreshold 比较结果的绝对值低于该值，将被视为0
     * @throws IOException
     * @since 2020.09.21
     */
    public static <T extends Comparable<T>> Set<Path> findSmallerPaths(Collection<Path> paths,
            Function<BasicFileAttributes, T> getter, Comparator<T> comparator, int largerThreshold) throws IOException {
        HashSet<Path> result = new HashSet<>();
        T max = null;
        Path maxPath = null;
        for (Path path : paths) {
            BasicFileAttributeView view = Files.getFileAttributeView(path, BasicFileAttributeView.class);
            T current = getter.apply(view.readAttributes());
            if (maxPath == null) {
                maxPath = path;
                max = current;
            } else {
                int compareTo = comparator.compare(current, max);
                if (Math.abs(compareTo) > largerThreshold) {
                    if (compareTo > 0) {
                        result.add(maxPath);
                        
                        maxPath = path;
                        max = current;    
                    } else if (compareTo < 0) {
                        result.add(path);
                    }
                }
            }
        }
        return result;
    }

}
