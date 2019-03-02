package indi.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.common.io.ByteStreams;

import indi.exception.RuntimeException2;

public class StreamUtils {

	/**
     * 将输入流转化为指定编码的字符串，默认为utf-8
     */
    public static String toString(InputStream inStream) {
        return toString(inStream, null);
    }

    /**
     * 将输入流转化为指定编码的字符串，默认为utf-8
     * 
     * <p>不会关闭输入流!
     */
    public static String toString(InputStream inStream, @Nullable String charset) {
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
