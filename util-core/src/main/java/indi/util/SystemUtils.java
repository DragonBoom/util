package indi.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wzh
 * @since 2019.12.07
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SystemUtils {

    /**
     * 判断当前系统是否为windows系统
     * 
     */
    public static boolean isWin() {
        String osName = System.getProperty("os.name");// Windows 10
        return osName.startsWith("Win");
    }

    /**
     * 修正可执行Jar包无法正确读取log4j2配置文件的问题（用Eclipse打包成可执行Jar包后，配置文件不是在默认路径，需要手动加载）
     * 
     * @since 2022.01.11
     */
    public static void correctRunnableJarLog4j2() throws URISyntaxException {
        // 以此判断是否发生了阻塞
        // 用windows命令行执行时，可能需要先执行chcp 65001以切换到utf8环境，并为java语句加上-Dfile.encoding=utf-8

        // 由于用Eclipse直接打包为Runnable
        // jar时日志的配置文件的路径不是默认的/src/main/resources，而是/resources，故用以下代码进行兼容
        String path = "resources/log4j2.xml";
        URL log4j2ConfUrl = SystemUtils.class.getClassLoader().getResource(path);

        if (log4j2ConfUrl != null) {
            LoggerContext logContext = (LoggerContext) LogManager.getContext(false);// false使得返回专用的logger
            logContext.setConfigLocation(log4j2ConfUrl.toURI());
            System.out.println("将使用可执行文件内的日志配置文件，请忽视找不到配置文件的报错");
        }
        System.out.println("file.encoding=" + System.getProperty("file.encoding"));
        System.out.println("Charset.defaultCharset()=" + Charset.defaultCharset());
    }
}
