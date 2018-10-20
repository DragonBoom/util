package indi.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectMapperUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static ObjectMapper getMapper() {
        return objectMapper;
    }
}
