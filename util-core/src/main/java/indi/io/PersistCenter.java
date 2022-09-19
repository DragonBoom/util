/**
 * 
 */
package indi.io;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import indi.bean.BeanUtils;
import indi.collection.CollectionUtils;
import indi.util.ReflectUtils;
import lombok.Getter;

/**
 * 持久化中心，将对象的属性持久化到文件中。本工具用于将动态的文件的数据与对象的属性相绑定，不适用于从静态文件中获取配置
 * 
 * <p>本类用于将指定对象的指定属性持久化到磁盘文件中，所指定属性需要实现getter、setter方法；
 * 本类作为抽象类，主要提供了从对象中获取属性的逻辑，具体的持久化/反持久化由子类实现，可能是json或base64等
 * 
 * <p>一个持久化文件只应有同一种读、写方式
 * 
 * <p>2021.02.03 添加注解@Persist，可用其声明所需持久化的属性，该模式与旧的通过构造函数声明的模式并存
 * 
 * @author wzh
 * @since 2020.09.04
 */
public abstract class PersistCenter {
    private static final String DEFAULT_PERSIST_FILE_NAME =  "persistCenter.json";// 本类不涉及json的逻辑
    private static final Class<? extends Annotation> PERSIST_FIELD_ANNOTATION = Persist.class;
    @Getter
    protected Path homePath;
    protected Path persistFilePath;
    protected String persistFileName;
    
    /** 需要持久化的对象 */
    protected Object obj;
    /** 需要持久化的对象的属性 */
    protected String[] propertyNames;
    protected PropertyDescriptor[] properties;
    
    
    public PersistCenter(Path homePath, String persistFileName, Object obj, String... propertyNames) {
        this.propertyNames = propertyNames;
        this.obj = obj;
        
        this.homePath = homePath;
        this.persistFileName = persistFileName;
        this.persistFilePath = homePath.resolve(persistFileName);
        
        initPropertyDescriptors(obj, propertyNames);
    }
    
    public PersistCenter(Path homePath, Object obj, String... propertyNames) {
        this(homePath, DEFAULT_PERSIST_FILE_NAME, obj, propertyNames);
    }
    
    private void initPropertyDescriptors(Object obj, String... propertyNames) {
        Objects.requireNonNull(obj);
        Class<? extends Object> targetClass = obj.getClass();
        Set<String> inited = new HashSet<>();// 用于校验是否重复注册
        // 每个属性只初始化一次
        // 1. 通过注解获取需持久化的属性
        List<Field> persistFields = ReflectUtils.listFieldAnnotations(targetClass, PERSIST_FIELD_ANNOTATION);
        properties = new PropertyDescriptor[persistFields.size() + propertyNames.length];
        for (int i = 0; i < persistFields.size(); i++) {
            Field field = persistFields.get(i);
            String name = field.getName();
            properties[i] = BeanUtils.getFullPropertyDescriptor(targetClass, name);
            // 校验能否序列化
            if (!(field.getType() instanceof Serializable)) {
                throw new IllegalArgumentException(String.format("%s.%s must instance of Serializable", targetClass, name));
            }
            inited.add(name);
        }

        // 2. 通过传入参数获取需持久化的属性
        for (int i = persistFields.size(); i < propertyNames.length; i++) {
            String name = propertyNames[i];
            if (inited.contains(name)) {
                throw new IllegalArgumentException("属性" + name + "已注册持久化，无法二次注册");
            }
            properties[i] = BeanUtils.getFullPropertyDescriptor(targetClass, name);
            // 校验能否序列化
            if (!(properties[i].getPropertyType() instanceof Serializable)) {
                throw new IllegalArgumentException(String.format("%s.%s must instance of Serializable", targetClass, name));
            }
            inited.add(name);
        }
        // 3. 校验
        if (CollectionUtils.isEmpty(properties)) {
            throw new IllegalArgumentException(String.format("无法在%s中获取所需持久化的属性", obj));
        }
    }
    
    /**
     * 持久化属性到文件中；将跳过值为null的属性
     * 
     * @throws Exception
     * @since 2021.07.03
     */
    public void persist() throws Exception {
        Map<String, Object> map = new HashMap<>();
        for (PropertyDescriptor property : properties) {
            String name = property.getName();
            Object oValue= BeanUtils.invokeGetter(property, obj);
            
            if (oValue == null) {
                continue;
            }
            map.put(name, oValue);
        }
        Map<String, Object> oldMap = null;
        if (!FileUtils.isFileEmpty(persistFilePath)) {
            oldMap = readFile();
        } else {
            oldMap = new HashMap<>();
        }
        FileUtils.createEmptyFileIfNotExist(persistFilePath);
        writeFile(map, oldMap);
    }
    
    /**
     * 将Map写入到文件中；建议实现为增量地修改，使得持久化文件可以被多个应用使用——可通过合并Map简单地实现该效果
     * 
     * <p>一个持久化文件只应有同一种读、写方式
     * 
     * @param newMap 从指定对象的指定属性中获得的映射，Map<变量名，属性值>
     * @param oldMap 从持久化文件中获得的映射（具体逻辑由子类实现），Map<变量名，属性值>；文件不存在时为空映射
     * @throws Exception
     * @author DragonBoom
     * @since 2020.09.04
     */
    protected abstract void writeFile(Map<String, Object> newMap, Map<String, Object> oldMap) throws Exception;
    
    /**
     * 从持久化文件中读取数据至对象属性（将跳过没有持久化到文件的属性）
     * 
     * @throws Exception
     * @since 2021.01.09
     */
    public void read() throws Exception {
        if (FileUtils.isFileEmpty(persistFilePath)) {
            return;
        }
        Map<String, Object> map = readFile();
        if (map == null) {
            return;
        }
        // 利用反射写入属性
        for (PropertyDescriptor property : properties) {
            String propertyName = property.getName();
            final Object fValue = map.get(propertyName);
            if (fValue != null) {
                Class<?> type = property.getPropertyType();
                // 仅当所需注入属性的类型是同类或父类时才执行注入
                if (type.isAssignableFrom(fValue.getClass())) {
                    BeanUtils.invokeSetter(property, obj, fValue);
                } else {
                    throw new IllegalArgumentException("格式错误：无法将 " + fValue.getClass() + " 转化为 " + type);
                }
            }
        }
    }
    
    /**
     * 从文件中获取Map结构。实现时，建议尽量保证一个持久化文件可用于多个应用，即在读取数据时保留无法识别的数据（而不是直接舍弃）
     * 
     * <p>一个持久化文件只应有同一种读、写方式
     * 
     * @return 必不为null
     * @throws Exception
     * @author DragonBoom
     * @since 2020.09.04
     */
    protected abstract Map<String, Object> readFile() throws Exception;
}
