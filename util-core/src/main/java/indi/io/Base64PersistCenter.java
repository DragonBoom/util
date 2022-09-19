/**
 * 
 */
package indi.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.type.TypeReference;

import indi.bean.ObjectMapperUtils;

/**
 * 持久化中心，将对象的属性持久化到文件中。
 * 
 * <p> 2020.09.04 将文件的数据结构，改为属性名 -> base64的json结构，使得可以存放复杂结构而不必担心反序列化失败；
 * 缺点是存放的不是明文，无法直接修改文件。
 * 
 * <p>注意，与将类持久化为字节数组相同，若修改了所持久化对象的路径，会导致无法反持久化（抛ClassNotFoundException）
 * 
 * <p>目前持久化文件中没有保存对象信息，因此同一个持久化文件内，无法同时存储来自不同对象的同名属性
 * 
 * @author wzh
 * @since 2020.09.02
 */
public class Base64PersistCenter extends PersistCenter {
    
    public Base64PersistCenter(Path homePath, String persistFileName, Object obj, String... propertyNames) {
        super(homePath, persistFileName, obj, propertyNames);
    }
    
    public Base64PersistCenter(Path homePath, Object obj, String... propertyNames) {
        super(homePath, obj, propertyNames);
    }
    
    /** key -> base64 */
    private static final TypeReference<ConcurrentHashMap<String, String>> STRING_MAP_TYPE_REF = 
            new TypeReference<ConcurrentHashMap<String, String>>() {};
    
    @Override
    protected void writeFile(Map<String, Object> newMap, Map<String, Object> oldMap) throws Exception {
        oldMap.putAll(newMap);
        Map<String, String> keyBase64Map = new HashMap<>();
        
        for (Entry<String, Object> entry : oldMap.entrySet()) {
            String key = entry.getKey();
            Object obj = entry.getValue();
            final String base64;
            try (ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream(); 
                    ObjectOutputStream outStream = new ObjectOutputStream(byteOutStream)) {
                outStream.writeObject(obj);
                base64 = Base64.getEncoder().encodeToString(byteOutStream.toByteArray());
            }
            keyBase64Map.put(key, base64);
        }
        
        String json = ObjectMapperUtils.getMapper().writeValueAsString(keyBase64Map);
        Files.write(persistFilePath, json.getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
       
    }
    
    /**
     * 将特定对象的指定属性与持久化文件同步；将用属性名作为获取该属性值的键
     * 
     * <li>若持久化文件中不存在键，不做任何修改
     * <li>若持久化文件中存在键，将把文件中的值写入对象中，覆盖属性原本的值
     * 
     * <p>支持的属性类型有：List、Map；不支持静态域
     * 
     * @param obj
     * @param propertyNames
     * @author DragonBoom

     * @since 2020.09.04
     */
    @Override
    protected Map<String, Object> readFile() throws Exception {
        // 尝试从文件中获取Map
        byte[] bytes = Files.readAllBytes(persistFilePath);
        Map<String, String> base64Map = ObjectMapperUtils.getMapper().readValue(bytes, STRING_MAP_TYPE_REF);
        
        if (base64Map == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        
        for (Entry<String, String> entry : base64Map.entrySet()) {
            String propertyName = entry.getKey();
            String base64 = entry.getValue();
            final Object fValue;
            try (ObjectInputStream inputStream = new ObjectInputStream(
                    Base64.getDecoder().wrap(new ByteArrayInputStream(base64.getBytes())))) {
                fValue = inputStream.readObject();
            }
            map.put(propertyName, fValue);
        }
        return map;
    }
}
