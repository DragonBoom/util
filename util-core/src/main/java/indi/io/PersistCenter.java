/**
 * 
 */
package indi.io;

import java.beans.PropertyDescriptor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import indi.bean.BeanUtils;
import lombok.Getter;

/**
 * @author wzh
 * @since 2020.09.04
 */
public abstract class PersistCenter {
    private static final String DEFAULT_PERSIST_FILE_NAME =  "persistCenter.json";
    @Getter
    protected Path homePath;
    protected Path persistFilePath;
    protected String persistFileName;
    
    protected Object obj;
    protected String[] propertyNames;
    
    
    public PersistCenter(Path homePath, String persistFileName, Object obj, String... propertyNames) {
        this.propertyNames = propertyNames;
        this.obj = obj;
        
        this.homePath = homePath;
        this.persistFileName = persistFileName;
        this.persistFilePath = homePath.resolve(persistFileName);
    }
    
    public PersistCenter(Path homePath, Object obj, String... propertyNames) {
        this(homePath, DEFAULT_PERSIST_FILE_NAME, obj, propertyNames);
        
    }
    
    public void persist() throws Exception {
        Map<String, Object> map = new HashMap<>();
        for (String propertyName : propertyNames) {
            Object oValue= BeanUtils.getProperty(obj, propertyName);
            
            if (oValue == null) {
                continue;
            }
            map.put(propertyName, oValue);
        }
        Map<String, Object> oldMap = null;
        if (Files.exists(persistFilePath)) {
            oldMap = readFile();
        } else {
            oldMap = new HashMap<>();
        }
        writeFile(map, oldMap);
    }
    
    /**
     * 将Map写入到文件中；建议实现增量持久化，而不是简单的覆盖，使得持久化文件可以被多个应用使用——可通过合并Map简单地实现该效果
     * 
     * <p>支持对部份类型的属性进行自动转化
     * 
     * @param map
     * @throws Exception
     * @author DragonBoom
     * @since 2020.09.04
     */
    protected abstract void writeFile(Map<String, Object> newMap, Map<String, Object> oldMap) throws Exception;
    
    public void read() throws Exception {
        Map<String, Object> map = null;
        if (Files.exists(persistFilePath)) {
            map = readFile();
        }
        if (map == null) {
            return;
        }
        // 利用反射
        for (String propertyName : propertyNames) {

            // 获取base64码，解析成对象
            final Object fValue = map.get(propertyName);
            if (fValue != null) {
                PropertyDescriptor propertyDescriptor = BeanUtils.getFullPropertyDescriptor(obj.getClass(), propertyName);
                Class<?> type = propertyDescriptor.getPropertyType();
                // 若类型不符，尝试进行转化
                if (!type.isAssignableFrom(fValue.getClass())) {
                    // 合并集合
                    if (Collection.class.isAssignableFrom(type) && fValue instanceof Collection) {
                        Collection<?> oValue = (Collection<?>) BeanUtils.invokeGetter(propertyDescriptor, obj);
                        oValue.addAll((Collection)fValue);
                    } else {
                        throw new IllegalArgumentException("格式错误：无法将 " + fValue.getClass() + " 转化为 " + type);
                    }
                } else {
                    BeanUtils.invokeSetter(propertyDescriptor, obj, fValue);
                }
            }
        }
    }
    
    /**
     * 从文件中获取Map结构
     * 
     * @return 比不为null
     * @throws Exception
     * @author DragonBoom
     * @since 2020.09.04
     */
    protected abstract Map<String, Object> readFile() throws Exception;
}
