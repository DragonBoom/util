package indi.bean;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectMapperUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    static {
        // 反序列化时忽视bean中不存在的属性
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static ObjectMapper getMapper() {
        return objectMapper;
    }
}
