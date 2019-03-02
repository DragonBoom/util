package indi.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 加密、解密相关的工具类
 */
public class EncryptUtils {
    private static final AtomicLong uniqueSequence = new AtomicLong();

    /**
     * 获取不重复的字符串
     * 
     * @param last 最近一次用该方法生成的字符串 
     */
    public synchronized static String getUnique(String last) {
        // TODO 暂时采用最简单的增长序列实现
        if (last != null) {
            uniqueSequence.set(Long.parseLong(last));
        }
        return new StringBuilder().append(uniqueSequence.incrementAndGet()).toString();
    }
}
