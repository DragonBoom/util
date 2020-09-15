/**
 * 
 */
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
 * 由于使用Map作为存储结构，序列化时直接对整个Map进行序列化，会导致反序列化时，数据结构不一致的情况：
 * 如Set的集合会被反序列化为List
 * 
 * @author wzh
 * @since 2020.09.04
 */
public class JsonPersistCenter extends PersistCenter{
    private static final TypeReference<ConcurrentHashMap<String, Object>> MAP_TYPE_REF = 
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
        byte[] bytes = Files.readAllBytes(persistFilePath);
        return ObjectMapperUtils.getMapper().readValue(bytes, MAP_TYPE_REF);
    }


}
