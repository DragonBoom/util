package indi.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableSet;

import lombok.SneakyThrows;

class ReflectUtilsTest {
    @Deprecated
    private String strField1;

    @Test
    @Disabled
    void testGetAllClasses() throws ClassNotFoundException, URISyntaxException, IOException {
        ImmutableSet<Class<?>> allClasses = ReflectUtils.getAllClasses("indi.core.util");
        System.out.println(allClasses);
    }
    
    @Test
    @Disabled
    @SneakyThrows
    void listFieldAnnotationsTest() {
        List<Field> fields = ReflectUtils.listFieldAnnotations(this.getClass(), Deprecated.class);
        System.out.println(fields);
        Assertions.assertEquals(this.getClass().getDeclaredField("strField1"), fields.get(0));
    }
}
