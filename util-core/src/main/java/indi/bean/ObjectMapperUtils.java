package indi.bean;

import java.text.SimpleDateFormat;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectMapperUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ObjectMapper formatDateMapper = new ObjectMapper();
    
    static {
        // 反序列化时忽视bean中不存在的属性
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        formatDateMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // 日期格式
        formatDateMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }

    public static ObjectMapper getMapper() {
        return objectMapper;
    }
    
    public static ObjectMapper getFormatDateMapper() {
        return formatDateMapper;
    }
}
