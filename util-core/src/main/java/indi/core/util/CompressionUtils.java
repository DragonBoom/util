package indi.core.util;

import java.io.IOException;

import lombok.Setter;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.UnzipParameters;

public class CompressionUtils {
    @Setter
    private static boolean enableLog = false;
    @Setter
    private static boolean enableErrorLog = true;
    
    public enum ResultTypes {
        PasswordError, Success, Others
    }

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
                    LoggerUtils.getLogger().error(errorReason);
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

    public static final ResultTypes unpackZip(String sourcePath, String destPath, String password) {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(sourcePath);
            zipFile.setPassword(password.toCharArray()); // 字符串位于常量池，无法做到用完即弃，用字符串存密码存在安全问题orz
            UnzipParameters params = new UnzipParameters();
            zipFile.getFileHeaders();
            zipFile.setFileNameCharset("");
            zipFile.extractAll(destPath, params);
        } catch (ZipException e) {
            if (e.getMessage().contains("Password")) {
                LoggerUtils.getLogger().info("密码错误");
                return ResultTypes.PasswordError;
            }
            e.printStackTrace();
            return ResultTypes.Others;
        }

        return ResultTypes.Success;
    }

}
