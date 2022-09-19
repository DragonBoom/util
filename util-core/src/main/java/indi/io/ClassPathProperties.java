/**
 * 
 */
package indi.io;

import java.io.InputStream;
import java.util.Properties;

import lombok.AccessLevel;
import lombok.Cleanup;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * 用于从类路径(classPath)中的properties获取数据的工具类
 * 
 * @author wzh
 * @since 2020.09.07
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClassPathProperties {

    /**
     * 从指定路径的properties文件中获取数据；可用于代码脱敏
     * 
     * <p>暂时采用简单实现，不考虑浪费性能问题
     * 
     * @param path 如 /application.properties
     * @param key
     * @return
     * @author DragonBoom
     * @since 2020.09.07
     */
    @SneakyThrows
    public static String getProperty(String path, String key) {
        Properties properties = new Properties();
        @Cleanup
        InputStream inStream = ClassPathProperties.class.getResourceAsStream(path);
        if (inStream != null) {
            properties.load(inStream);
            return properties.getProperty(key);
        } else {
            log.warn("找不到配置文件：{}，无法获得配置项：{}", path, key);
            return null;
        }
    }
}
