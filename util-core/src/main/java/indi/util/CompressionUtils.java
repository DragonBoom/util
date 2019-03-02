package indi.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import indi.exception.WrapperException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.UnzipParameters;
import net.lingala.zip4j.model.ZipParameters;

@Slf4j
public class CompressionUtils {
    @Setter
    private static boolean enableLog = false;
    @Setter
    private static boolean enableErrorLog = true;
    
    public enum ResultTypes {
        PasswordError, Success, Others
    }

    /**
     * 
     * @param sourcePath
     * @param destPath
     * @param password
     * @return
     */
    public static final ResultTypes unpackRar(String sourcePath, String destPath, String password) {
        // check
        StringBuilder sb = new StringBuilder("rar")
                .append(" x")
                .append(" -p").append(password)
                .append(" -o-") // 覆盖已存在项目
                .append(" -isnd") // 禁用声音
                .append(" \"").append(sourcePath).append("\" \"").append(destPath).append("\""); // 设置路径
        System.out.println(sb);
        Process exec = null;
        try {
            exec = Runtime.getRuntime().exec(sb.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 若解压出错
        try {
            if (exec.getErrorStream().read() != -1) {
                String errorReason = StreamUtils.toString(exec.getErrorStream(), "gbk");
                if (enableErrorLog) {
                    log.error(errorReason);
                }
                if (errorReason.contains("密码")) {
                    return ResultTypes.PasswordError;
                }
                System.out.println(exec.exitValue());
               
                return ResultTypes.Others;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (enableLog) {
            System.out.println(exec.getErrorStream());
            System.out.println(StreamUtils.toString(exec.getInputStream(), "gbk"));
        }
        return ResultTypes.Success;
    }

    /**
     * 
     * @param sourcePath
     * @param destPath
     * @param password
     * @return
     */
    public static final ResultTypes unpackZip(String sourcePath, String destPath, String password) {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(sourcePath);
            zipFile.setPassword(password.toCharArray()); // by doc: 字符串位于常量池，无法做到用完即弃，用字符串存密码存在安全问题orz
            UnzipParameters params = new UnzipParameters();
            zipFile.getFileHeaders();
            zipFile.setFileNameCharset("");
            zipFile.extractAll(destPath, params);
        } catch (ZipException e) {
            if (e.getMessage().contains("Password")) {
                log.info("密码错误");
                return ResultTypes.PasswordError;
            }
            e.printStackTrace();
            return ResultTypes.Others;
        }

        return ResultTypes.Success;
    }
    
    /**
     * 将源路径对应的文件/目录压缩为zip文件，并写入到给定路径
     * 
     * @param sourcePathStr 源路径
     * @param destZipPathStr 目标压缩文件路径
     * @param overwrite 若目标文件已存在，是否进行覆盖；若否，则但目标文件已存在时将会跑异常
     */
    public static final ZipFile packZip(String sourcePathStr, String destZipPathStr, boolean overwrite) {
        Path sourcePath = Paths.get(sourcePathStr);
        Path destZipPath = Paths.get(destZipPathStr);
        
        // 0. 校验
        // a. 校验源路径是否存在
        if (!Files.exists(sourcePath, LinkOption.NOFOLLOW_LINKS)) {
            throw new IllegalArgumentException("路径不存在： " + sourcePathStr);
        }
        // b. 校验目标文件是否存在
        if (Files.exists(destZipPath, LinkOption.NOFOLLOW_LINKS)) {
            if (overwrite) {
                try {
                    Files.delete(destZipPath);
                } catch (IOException e) {
                    throw new WrapperException(e);
                }
            } else {
                throw new IllegalArgumentException("目标文件已存在： " + sourcePathStr);
            }
        }
        
        try {
            ZipFile zipFile = new ZipFile(destZipPathStr);
            if (Files.isDirectory(sourcePath, LinkOption.NOFOLLOW_LINKS)) {
                // 若源路径是目录
                // 找出所有文件
                List<File> files = Files.list(sourcePath)
                        .filter(entry -> {
                            return !Files.isDirectory(entry, LinkOption.NOFOLLOW_LINKS);// 过滤掉文件夹
                        })
                        .map(Path::toFile)
                        .collect(Collectors.toList());
                // 转化为ArrayList
                ArrayList<File> fileArrayList = new ArrayList<>();
                fileArrayList.addAll(files);
                
                log.debug("将文件夹压缩为ZIP文件: {} {}", destZipPathStr, fileArrayList);
                
                zipFile.createZipFile(fileArrayList, new ZipParameters());
            } else {
                log.debug("将文件压缩为ZIP文件: {}", destZipPathStr);
                
                // 若源路径不是目录
                zipFile.createZipFile(sourcePath.toFile(), new ZipParameters());
            }
            return zipFile;
        } catch (ZipException e) {
            throw new WrapperException(e);
        } catch (IOException e) {
            throw new WrapperException(e);
        }
    }
    
    /**
     * 压缩源文件/目录并写入到给定目录下
     * 
     * @param targetFileName 压缩后的文件名
     * @return
     */
    public static final ZipFile packZip2Dir(String sourcePath, String targetFileName, String destDir, boolean overwrite) {
        return packZip(sourcePath, Paths.get(destDir, targetFileName).toString(), overwrite);
    }
    
    /**
     * 压缩源文件/目录并写入到给定目录下的同名文件中
     * 
     * @return
     */
    public static final ZipFile packZip2Dir(String sourcePath, String destDir, boolean overwrite) {
        Path path = Paths.get(sourcePath);
        // buil zip file name
        String targetFileName = new StringBuilder(path.getFileName().toString()).append(".zip").toString();
        return packZip2Dir(sourcePath, targetFileName, destDir, overwrite);
    }

}
