package indi.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

import indi.exception.RuntimeException2;

public class ReflectUtils {

    public static ImmutableSet<Class<?>> getAllClasses(String packageName)
            throws URISyntaxException, IOException, ClassNotFoundException {
        packageName = formatPackageName(packageName);
        // get package url
        URL url = Thread.currentThread().getContextClassLoader().getResource(packageName);
        Path path = Paths.get(url.toURI());
        if (!Files.isDirectory(path)) {
            throw new IOException("文件夹不存在! " + path);
        }
        DirectoryStream<Path> stream = Files.newDirectoryStream(path);
        Builder<Class<?>> builder = ImmutableSet.builder();
        for (Path p : stream) {
            int count = p.getNameCount();
            int aimIndex = -1;
            for (int i = 0; i < count; i++) {
                String name = p.getName(i).toString();
                if (name.equals("classes") || name.equals("test-classes")) {
                    aimIndex = i + 1;
                    break;
                }
            }
            if (aimIndex == -1) {
                throw new ClassNotFoundException("路径错误，找不到classes文件夹");
            }
            Path subpath = p.subpath(aimIndex, p.getNameCount());

            String fileName = subpath.toString().replace('\\', '.');
            String className = fileName.substring(0, fileName.lastIndexOf('.'));
            Class<?> result = Class.forName(className.toString());
            builder.add(result);
        }
        return builder.build();
    }

    public static String getRootLocation() {
        URL url = Thread.currentThread().getContextClassLoader().getResource("//");
        String fileStr = url.toString();
        // file:/E:/Github/toolkit/target/classes/
        return fileStr.substring(6, fileStr.length() - 1);// E:/Github/toolkit/target/classes
    }

    private static String formatPackageName(String packageName) {
        if (packageName.contains(".")) {
            return packageName.replace('.', '\\');
        } else if (packageName.contains("\\")) {
            return packageName;
        }
        throw new RuntimeException2(new StringBuilder("包名格式错误:").append(packageName).toString());
    }
}
