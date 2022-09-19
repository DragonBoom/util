package indi.util;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.common.collect.Lists;

import indi.collection.CollectionUtils;
import indi.test.TestSeparateExtension;

@ExtendWith(TestSeparateExtension.class)
class CollectionUtilsTest {

    @Test
//    @Disabled
    void testListSameElemetns() {
        List<String> sameElements = CollectionUtils.listSameElements(Lists.newArrayList("ww", "ff", "ff", "ff", "cc"),
                Lists.newArrayList("ff", "cc", "aa"), null);
        System.out.println(sameElements);
        Assertions.assertEquals(Lists.newArrayList("ff", "cc"), sameElements);
    }
    

}
