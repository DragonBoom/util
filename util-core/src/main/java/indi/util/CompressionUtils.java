package indi.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import indi.data.Three;
import indi.exception.WrapperException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.UnzipParameters;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

@Slf4j
public class CompressionUtils {
    
    public enum ResultTypes {
        PASSWORD_ERROR, SUCCESS, OTHERS
    }

    /**
     * 解压rar压缩文件（只支持rar格式的文件；后缀可以不是.rar）
     * 
     * @param sourcePath
     * @param destPath
     * @param password
     * @return 只判断解压是否成功
     */
    @SneakyThrows
    public static final ResultTypes unpackRar(String sourcePath, String destPath, String password) {
        // check
        StringBuilder sb = new StringBuilder("rar ")
                .append("x ")
                .append("-o- ") // 覆盖已存在项目
                .append("-isnd ") // 禁用声音
                .append("\"").append(sourcePath).append("\" \"").append(destPath).append("\" "); // 设置路径
        if (!StringUtils.isEmpty(password)) {
            sb.append("-p").append(password).append(" ");
        }
        log.debug("Process command: {}", sb);
        Three<String, String, Integer> result = ProcessUtils.process(sb.toString());
        Integer exitValue = result.getThird();
        return exitValue == 0 ? ResultTypes.SUCCESS : ResultTypes.OTHERS;
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
                return ResultTypes.PASSWORD_ERROR;
            }
            e.printStackTrace();
            return ResultTypes.OTHERS;
        }

        return ResultTypes.SUCCESS;
    }
    
    /**
     * 将源路径对应的文件/目录压缩为zip文件，并写入到给定路径
     * 
     * <p>暂时仅支持单一目录的压缩
     * 
     * @param sourcePathStr 源路径
     * @param destZipPathStr 目标压缩文件路径
     * @param pwd 密码，可为空表示无密码
     * @param compressionLevel 压缩级别，可选范围 0-9，可为空表示取默认值
     * @param overwrite 若目标文件已存在，是否进行覆盖；若否，则当目标文件已存在时将会跑异常
     */
    public static final ZipFile packZip(String sourcePathStr, String destZipPathStr, @Nullable String pwd, Integer compressionLevel, boolean overwrite) {
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
        
        ZipParameters zipParameters = new ZipParameters();// 压缩参数
        // 压缩密码（用字符串存放密码存在被解析内存从而获取到的隐患。。。）
        Optional.ofNullable(pwd).ifPresent(args -> {
            zipParameters.setEncryptFiles(true);
            zipParameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
            zipParameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
            zipParameters.setPassword(pwd);
        });
        // 压缩方法 取默认的 Deflate
        
        // 压缩级别，可选范围为0-9
        Optional.ofNullable(compressionLevel).ifPresent(args -> zipParameters.setCompressionLevel(compressionLevel));
        
        try {
            ZipFile zipFile = new ZipFile(destZipPathStr);
            if (Files.isDirectory(sourcePath, LinkOption.NOFOLLOW_LINKS)) {
                // 若源路径是目录
                // 找出所有文件
                final List<File> files;
                try (Stream<Path> stream = Files.list(sourcePath)) {
                    files = stream
                            .filter(entry -> !Files.isDirectory(entry, LinkOption.NOFOLLOW_LINKS))// 过滤掉文件夹
                            .map(Path::toFile)
                            .collect(Collectors.toList());
                }
                // 转化为ArrayList
                ArrayList<File> fileArrayList = new ArrayList<>();
                fileArrayList.addAll(files);
                
                log.debug("将文件夹压缩为ZIP文件: {} , 文件数={}", destZipPathStr, fileArrayList.size());
                
                zipFile.createZipFile(fileArrayList, zipParameters);
            } else {
                log.debug("将文件压缩为ZIP文件: {}", destZipPathStr);
                
                // 若源路径不是目录
                zipFile.createZipFile(sourcePath.toFile(), zipParameters);
            }
            return zipFile;
        } catch (ZipException | IOException e) {
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
        return packZip(sourcePath, Paths.get(destDir, targetFileName).toString(), null, null, overwrite);
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
