package indi.util;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

import indi.collection.CollectionUtils;

class CollectionUtilsTest {

    @Test
    void testListSameElemetns() {
        List<String> sameElements = CollectionUtils.listSameElements(Lists.newArrayList("ww", "ff", "ff", "ff", "cc"),
                Lists.newArrayList("ff"), null);
        System.out.println(sameElements);
    }

}
