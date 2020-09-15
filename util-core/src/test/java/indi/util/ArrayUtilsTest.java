package indi.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ArrayUtilsTest {
    private String[] strArray1 = new String[] { "a", "b", "c" };
    private int[] intArray1 = new int[] { 1, 2, 3 };
    private Integer[] integerArray1 = new Integer[] { 1, 2, 3 };

    @Test
    void test() {
        String[] newArray = ArrayUtils.copyOf(strArray1, 7, "w");
        Assertions.assertArrayEquals(new String[] { "a", "b", "c", "w", "w", "w", "w" }, newArray);

        String[] truncatedArray = ArrayUtils.truncateSuffix(newArray, "w");
        Assertions.assertArrayEquals(new String[] { "a", "b", "c" }, truncatedArray);

        Integer[] newArray2 = ArrayUtils.copyOf(integerArray1, 12, 2);// compile pass
        Assertions.assertArrayEquals(new Integer[] { 1, 2, 3, 2, 2, 2, 2, 2, 2, 2, 2, 2 }, newArray2);
        
//        ArrayUtils.copyOf(intArray1, 1, 1);// 该行无法编译通过：泛型参数无法传入基础数据类型！！
    }

}
