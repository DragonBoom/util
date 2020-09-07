/**
 * 
 */
package indi.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.zip.CRC32;

import indi.exception.WrapperException;
import indi.io.FileUtils;

/**
 * 校验用工具类
 * 
 * @author wzh
 * @since 2020.09.05
 */
public class CheckUtils {

    /**
     * 获取指定文件的CRC32校验码（循环冗余校验码）。相较于MD5，CRC32的计算速度更快，适合不需要加密的场景
     * 
     * @param path
     * @return
     * @author DragonBoom
     * @since 2020.09.05
     */
    public static String getCRC32(Path path) {
        if (!FileUtils.isFile(path)) {
            throw new IllegalArgumentException("文件不存在或不是文件: " + path);
        }
        // 计算CRC32值
        CRC32 crc32 = new CRC32();
        ByteBuffer buffer = ByteBuffer.allocate(2048);
        try (FileChannel fc = FileChannel.open(path)) {
            buffer.clear();
            fc.read(buffer);
            buffer.flip();
            crc32.update(buffer);// 不断通过update方法传入字节数组，即可增量地更新CRC32码
        } catch (IOException e) {
            throw new WrapperException(e);
        }

        return Long.toString(crc32.getValue());
    }
    
}
