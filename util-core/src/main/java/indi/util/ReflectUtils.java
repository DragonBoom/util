package indi.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.collect.ImmutableSet;

import indi.exception.RuntimeException2;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReflectUtils {

    /**
     * 获取特定包下的所有类
     * 
     * @author DragonBoom
     * @param packageName 包名，支持indi.util或indi\\util两种写法（后者为标准写法）
     * @return
     * @throws URISyntaxException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static ImmutableSet<Class<?>> getAllClasses(String packageName)
            throws URISyntaxException, IOException, ClassNotFoundException {
        // 格式化包名
        packageName = formatPackageName(packageName);
        // 获取类加载器，并通过getResource方法获取包的路径（绝对路径）
        URL url = Thread.currentThread().getContextClassLoader().getResource(packageName);
        if (url == null) {
            throw new ClassNotFoundException("包不存在：" + packageName);
        }
        // 以文件夹的形式扫描包的路径（绝对路径）
        Path path = Paths.get(url.toURI());
        if (!Files.isDirectory(path)) {
            throw new IOException("该包名不是指向文件夹： " + path);
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            ImmutableSet.Builder<Class<?>> builder = ImmutableSet.builder();
            
            int beginIndex = -1;// 绝对路径中，类的全称的起始结点
            for (Path p : stream) {
                if (beginIndex == -1) {
                    // 获取类名：
                    // a. 寻找classes文件夹的结点下标
                    int count = p.getNameCount();
                    for (int i = 0; i < count; i++) {
                        String name = p.getName(i).toString();
                        if (name.equals("classes") || name.equals("test-classes")) {
                            beginIndex = i + 1;
                            break;
                        }
                    }
                    if (beginIndex == -1) {
                        throw new ClassNotFoundException("路径错误，找不到classes文件夹");
                    }
                }
                // b. 截取以classes文件夹为起点的类的文件路径
                Path subpath = p.subpath(beginIndex, p.getNameCount());
                // c. 将文件路径转化为类的全名，主要是将【\】转化为【.】，并去掉后缀
                String fileName = subpath.toString().replace('\\', '.');
                String className = fileName.substring(0, fileName.lastIndexOf('.'));
                Class<?> result = Class.forName(className);
                // 记录至返回的ImmutableSet中
                builder.add(result);
            }
            return builder.build();
        }
    }

    /**
     * 获取项目的根路径，绝对路径，如：E:/Github/toolkit/target/classes/
     * 
     * @author DragonBoom
     * @since 2020.02.23
     * @return
     */
    public static String getRootLocation() {
        URL url = Thread.currentThread().getContextClassLoader().getResource("//");
        String fileStr = url.toString();
        // file:/E:/Github/toolkit/target/classes/
        return fileStr.substring(6, fileStr.length() - 1);// E:/Github/toolkit/target/classes
    }

    /**
     * 格式化包名，java反射只认标准写法
     * 
     * @author DragonBoom
     * @param packageName
     * @return
     */
    private static String formatPackageName(String packageName) {
        if (packageName.contains(".")) {
            return packageName.replace('.', '\\');
        } else if (packageName.contains("\\")) {
            return packageName;
        }
        throw new RuntimeException2(new StringBuilder("包名格式错误:").append(packageName).toString());
    }
    
}
