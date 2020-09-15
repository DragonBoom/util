package indi.bean;

import java.text.SimpleDateFormat;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ObjectMapperUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    /** 以特定格式输出/输入日期（而不是默认的毫秒）的ObjectMapper */
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
    
    /**
     *  以特定格式输出/输入日期（而不是默认的毫秒）的ObjectMapper
     * 
     * @return
     * @author DragonBoom
     * @since 2020.09.12
     */
    public static ObjectMapper getFormatDateMapper() {
        return formatDateMapper;
    }
}
