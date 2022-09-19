package indi.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.common.io.ByteStreams;

import indi.exception.RuntimeException2;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IOUtils {

    /**
     * 获取ByteBuffer的字节数组
     * 
     * @param byteBuffer
     * @return
     * @author DragonBoom
     * @since 2020.09.18
     */
    public static final byte[] readBytes(ByteBuffer byteBuffer) {
        byte[] bytes = null;
        if (byteBuffer.hasArray()) {
            bytes = byteBuffer.array();
        } else {
            bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);
        }
        return bytes;
    }
    
    /**
     * 将输入流转化为指定编码的字符串，默认为utf-8
     */
    public static final String toString(InputStream inStream) {
        return toString(inStream, StandardCharsets.UTF_8);
    }

    /**
     * 将输入流转化为指定编码的字符串，默认为utf-8
     * 
     * <p>不会关闭输入流!
     * 
     * @param charset 若使用java标准编码，请使用StandardCharsets指定 
     */
    public static final String toString(InputStream inStream, @Nullable Charset charset) {
        Objects.requireNonNull(inStream);
        /*
         * InputStream 与 BufferedInputStream 的区别，主要只在于read()方法上一个没有预缓存数据，一个有；
         * 因此，只要只用read(byte[], int, int)方法，就没必要用BufferedInputSream
         */
        byte[] bytes = null;
        try {
            bytes = ByteStreams.toByteArray(inStream);// 直接使用guava的工具
        } catch (IOException e2) {
            throw new RuntimeException2(e2);
        }
        return new String(bytes, Optional.ofNullable(charset).orElse(StandardCharsets.UTF_8));
    }
}
