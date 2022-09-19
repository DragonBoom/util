package indi.io;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.core.type.TypeReference;

import indi.bean.ObjectMapperUtils;

/**
 * 持久化中心，将对象的属性持久化到文件中。
 * 
 * <p>使用Map作为存储结构，序列化时直接对整个Map进行序列化，会导致当属性不是特定结构时，反序列化时将出现数据结构不一致的问题，
 * 如Set的集合会被反序列化为ArrayList；因此，只能持久化ObjectMapper反序列化时默认选择的类型
 * 
 * <p>持久化文件的格式为属性名-> json字符串的json文本
 * 
 * <p>目前持久化文件中没有保存对象信息，因此同一个持久化文件内，无法同时存储来自不同对象的同名属性
 * 
 * @author wzh
 * @since 2020.09.04
 */
public class JsonPersistCenter extends PersistCenter{
    private static final TypeReference<ConcurrentHashMap<String, Object>> COMMON_MAP_TYPE_REF = 
            new TypeReference<ConcurrentHashMap<String, Object>>() {};

    public JsonPersistCenter(Path homePath, Object obj, String... propertyNames) {
        super(homePath, obj, propertyNames);
    }
    
    public JsonPersistCenter(Path homePath, String persistFileName, Object obj, String... propertyNames) {
        super(homePath, persistFileName, obj, propertyNames);
    }

    @Override
    protected void writeFile(@Nonnull Map<String, Object> newMap, @Nonnull Map<String, Object> oldMap) throws Exception {
        // 合并Map
        oldMap.putAll(newMap);
        
        String str = ObjectMapperUtils.getMapper()
                .writerWithDefaultPrettyPrinter()// 美化输出结果
                .writeValueAsString(oldMap);
        Files.write(persistFilePath, str.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    @Override
    protected Map<String, Object> readFile() throws Exception {
        return ObjectMapperUtils.getMapper().readValue(Files.readAllBytes(persistFilePath), COMMON_MAP_TYPE_REF);
    }


}
