package indi.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Optional;

public class StreamUtils {

    public static String toString(InputStream inStream) {
        return toString(inStream, null);
    }

    /**
     * 将流转化为指定类型字符串，默认为utf-8
     */
    public static String toString(InputStream inStream, String charset) {
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(inStream, Optional.ofNullable(charset).orElse("utf-8"));
        } catch (UnsupportedEncodingException e1) {
            throw new RuntimeException(e1);
        }
        BufferedReader br = new BufferedReader(inputStreamReader);
        StringBuilder sb = new StringBuilder();
        String buffer = null;
        try {
            while ((buffer = br.readLine()) != null) {
                sb.append(buffer);
                sb.append("\n");// orz
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }
    
}
