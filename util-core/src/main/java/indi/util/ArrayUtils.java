package indi.util;

import java.util.Arrays;

import com.google.common.base.Objects;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ArrayUtils {

    /**
     * 类似Arrays.copyOf(source, newLength)，裁剪或扩充数组，在其基础上增加了指定填充值的功能
     * 
     * <p>复制指定的数组，截断或填充NULL（如有必要），以使副本具有指定的长度。
     * 
     * @param <T>
     * @param source
     * @param newLength
     * @param paddingValue 填充值
     * @return
     */
    public static <T> T[] copyOf(T[] source, int newLength, T paddingValue) {
        // 裁剪或扩容
        T[] newArray = Arrays.copyOf(source, newLength);// 填充值不稳定。。。
        // 填充
        if (source.length < newLength) {
            Arrays.fill(newArray, source.length, newArray.length, paddingValue);
        }
        
        return newArray;
    }
    
    /**
     * byte（基础数据类型）版本。。。
     * 
     * @param <T>
     * @param source
     * @param newLength
     * @param paddingValue
     * @return
     */
    public static byte[] copyOf(byte[] source, int newLength, byte paddingValue) {
        // 裁剪或扩容
        byte[] newArray = Arrays.copyOf(source, newLength);// 填充值不稳定。。。
        // 填充
        if (source.length < newLength) {
            Arrays.fill(newArray, source.length, newArray.length, paddingValue);
        }
        
        return newArray;
    }
    
    /**
     * 截取数组，获得不是以特定值结尾的新数组。可用于清理数组中无意义占位符。
     * 
     * <p>该方法无法传入基础数据类型，当T为基础数据类型时，参数T[]确实是基础数据类型数组，但参数T却是其对应的对象...
     * 
     * @param <T>
     * @param source
     * @param truncateSuffix
     * @return
     */
    public static <T> T[] truncateSuffix(T[] source, T suffix) {
        int newLen = source.length;
        for (int i = source.length - 1; i >= 0; i--) {
            if (Objects.equal(source[i], suffix)) {
                newLen--;
            } else {
                break;
            }
        }
        return Arrays.copyOf(source, newLen);
    }
    
    /**
     * 应对truncateSuffix方法无法传入byte（基础数据类型）参数的方法
     * 
     * @param source
     * @param suffix
     * @return
     */
    public static byte[] truncateSuffix(byte[] source, byte suffix) {
        int newLen = source.length;
        for (int i = source.length - 1; i >= 0; i--) {
            if (Objects.equal(source[i], suffix)) {
                newLen--;
            } else {
                break;
            }
        }
        return Arrays.copyOf(source, newLen);
    }
}
