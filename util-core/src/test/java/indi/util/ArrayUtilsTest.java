package indi.util;

import org.junit.jupiter.api.Test;

import indi.test.Prints;

class ArrayUtilsTest {
    private String[] strArray1 = new String[] {"a", "b", "c"};
    private int[] intArray1 = new int[] {1, 2, 3};
    private Integer[] integerArray1 = new Integer[] {1, 2, 3};

    @Test
    void test() {
        String[] newArray = ArrayUtils.copyOf(strArray1, 7, "w");
        Prints.printArray(newArray);// print: [a, b, c, w, w, w, w]
        
        
        String[] truncatedArray = ArrayUtils.truncateSuffix(newArray, "w");
        Prints.printArray(truncatedArray);
        
        // test for int array
        
//        ArrayUtils.copyOf(intArray1, 12, 2);// compile error
        ArrayUtils.copyOf(integerArray1, 12, 2);// compile pass
    }

}
