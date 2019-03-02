package indi.util;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableSet;

import indi.util.ReflectUtils;

class ReflectUtilsTest {

    @Test
    void testGetAllClasses() throws ClassNotFoundException, URISyntaxException, IOException {
        ImmutableSet<Class<?>> allClasses = ReflectUtils.getAllClasses("indi.core.util");
        System.out.println(allClasses);
    }

}
