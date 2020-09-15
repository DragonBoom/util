package indi.util;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * 
 * @author DragonBoom
 */
public class HashUtils {

    /**
     * 计算给定字节数组的哈希值。
     * 
     * <p>算法思路： 长度 + Object.hash() + 字符串的哈希算法（s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]）
     * 
     * 
     * 
     * @param bytes
     * @return
     */
    public int encode(byte[] bytes) {
        int len = bytes.length;
        int objHash = Arrays.hashCode(bytes);
        System.out.println(objHash);
        
        // h = new String(bytes).hashCode() 
        int h = 0;
        for (int i = 0; i < len; i++) {
            h = h * 31 + bytes[i];
        }
        
        String hashStr = new StringBuilder().append(len).append(objHash).append(h).toString();
        return Integer.parseInt(hashStr);// 需要返回Integer对象可用 Integer.valueOf(...)
    }
    
    @Test
    void go() {
        encode("ff".getBytes());
        System.out.println("ff".hashCode());
    }
}
