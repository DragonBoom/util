/**
 * 
 */
package indi.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Objects;
import java.util.zip.CRC32;

import indi.exception.WrapperException;
import indi.io.FileUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 校验用工具类
 * 
 * @author wzh
 * @since 2020.09.05
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CheckUtils {

    /**
     * 校验路径是否指向文件，存在报错只报第一个路径的问题
     *  
     * @param paths
     * @since 2021.01.08
     */
    private static void checkFile(Path... paths) {
        for (Path path : paths) {
            if (!FileUtils.isFile(path)) {
                throw new IllegalArgumentException("文件不存在或不是文件: " + path);
            }
        }
    }

    /**
     * 获取指定文件的CRC32校验码（循环冗余校验码）。相较于MD5，CRC32的计算速度更快，适合不需要加密的场景
     * 
     * @param path
     * @return
     * @author DragonBoom
     * @since 2020.09.05
     */
    public static String getCRC32(Path path) {
        checkFile(path);
        // 计算CRC32值
        CRC32 crc32 = new CRC32();
        ByteBuffer buffer = ByteBuffer.allocate(2048);
        try (FileChannel fc = FileChannel.open(path)) {
            while (fc.read(buffer) > -1) {
                buffer.flip();
                crc32.update(buffer);// 不断通过update方法传入字节数组，即可增量地更新CRC32码
                buffer.clear();
            }
        } catch (IOException e) {
            throw new WrapperException(e);
        }

        return Long.toString(crc32.getValue());
    }
    
    /**
     * 比较文件是否完全相同，将逐个字节进行比较
     * 
     * <p>不确定比较相同的两个文件时，是该方法快，还是先计算crc32后再比较更快
     * 
     * @param p1
     * @param p2
     * @return 相同返回0，否则返回-1
     * @since 2021.01.08
     */
    public static int compare(Path p1, Path p2) {
        checkFile(p1, p2);
        ByteBuffer buffer1 = ByteBuffer.allocate(2048);
        ByteBuffer buffer2 = ByteBuffer.allocate(2048);
        // 通过缓存读取数据以提高性能，而不是真的获取一个字节比较一个字节（避免频繁不连续访问）
        try (FileChannel fc1 = FileChannel.open(p1); FileChannel fc2 = FileChannel.open(p2)) {
            while (fc1.read(buffer1) > -1 && fc2.read(buffer2) > -1) {
                buffer1.flip();
                buffer2.flip();
                if (buffer1.limit() != buffer2.limit()) {
                    return -1;
                }
                while (buffer1.position() < buffer1.limit()) {
                    if (!Objects.equals(buffer1.get(), buffer2.get())) {
                        return -1;
                    }
                }
                buffer1.clear();
                buffer2.clear();
            }
        } catch (IOException e) {
            throw new WrapperException(e);
        }
        
        return 0;
    }    
}
