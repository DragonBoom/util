package indi.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.common.io.ByteStreams;

import indi.exception.RuntimeException2;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IOUtils {

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
        return toString(inStream, null);
    }

    /**
     * 将输入流转化为指定编码的字符串，默认为utf-8
     * 
     * <p>不会关闭输入流!
     */
    public static final String toString(InputStream inStream, @Nullable String charset) {
        byte[] bytes = null;
        try {
            bytes = ByteStreams.toByteArray(inStream);// 直接使用guava的工具
        } catch (IOException e2) {
            throw new RuntimeException2(e2);
        }
        try {
            return new String(bytes, Optional.of(charset).orElse("utf-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException2(e);
        }
    }
}
