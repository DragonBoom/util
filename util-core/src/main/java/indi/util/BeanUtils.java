package indi.util;

import static java.util.Locale.ENGLISH;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import indi.exception.WrapperException;
import lombok.Data;

public class BeanUtils {

	/**
	 * 将bean转化为新的Map<String, Object>。值为null的属性将被忽略。
	 */
	public static final Map<String, Object> createMap(Object bean) {
		return createMap(bean, true);
	}

	/**
	 * 将bean转化为新的Map<String, Object>
	 * 
	 * @param ignoreNull 若为true，将忽视值位null的属性
	 */
	public static final Map<String, Object> createMap(Object bean, boolean ignoreNull) {
	    // 获取每个字段的PropertyDescriptor，据此操作getter/setter方法
		List<PropertyDescriptor> propertyDescriptors = getPropertyDescriptor(bean);
		
		HashMap<String, Object> map = new HashMap<>();
		for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
			String name = propertyDescriptor.getName();

			if (name.equals("class")) {// 跳过class属性
				continue;
			}
			
			// 调用getter方法
			Object invokeResult = invokeGetter(propertyDescriptor, bean);

			if (ignoreNull && invokeResult == null) {
				continue;
			}
			// add 2 map
			map.put(name, invokeResult);
		}

		return map;
	}
	
	/**
	 * 获取bean的所有PropertyDescriptor。<br>
	 * PropertyDescriptor可用于操作getter/setter。<br>
	 * 该方法基由java自带代码实现，没有调用其他框架。
	 * 
	 * <p>注意，本方法将忽视class属性(即使每个对象都有getClass方法)！
	 * 
	 * @param bean
	 * @return
	 */
	public static final List<PropertyDescriptor> getPropertyDescriptor(Object bean) {
		BeanInfo beanInfo = null;
		try {
			beanInfo = Introspector.getBeanInfo(bean.getClass());
		} catch (IntrospectionException e) {
			throw new WrapperException(e);
		}
		
		// 排除class属性
		List<PropertyDescriptor> propertyDescriptors = Arrays.stream(beanInfo.getPropertyDescriptors())
		        .filter(propertyDescriptor -> {
		            return !"class".equals(propertyDescriptor.getName());
		        })
		        .collect(Collectors.toList());
		propertyDescriptors.removeIf(propertyDescriptor -> {
		    return "class".equals(propertyDescriptor.getName());
		});
		return propertyDescriptors;
	}
	
	/**
	 * 简单的工具，调用getter方法
	 */
	public static final Object invokeGetter(PropertyDescriptor propertyDescriptor, Object bean) {
	    // 调用getter方法
        Method getter = propertyDescriptor.getReadMethod();
        
        if (getter == null) {
            return null;
        }
        
        try {
            return getter.invoke(bean);
        } catch (Throwable e) {
            throw new WrapperException("Invoke getter error in " + bean.getClass() + " for property " + 
                    propertyDescriptor.getDisplayName() + " because " +  e.getMessage());
        }
	}
	
	/**
	 * 简单的工具，调用setter方法
	 */
	public static final Object invokeSetter(PropertyDescriptor propertyDescriptor, Object bean, Object value) {
	    // 调用setter方法
	    Method setter = propertyDescriptor.getWriteMethod();
	    
	    if (setter == null) {
	        return null;
	    }
	    
	    try {
	        return setter.invoke(bean, value);
	    } catch (Throwable e) {
	        throw new WrapperException("Invoke setter error in " + bean.getClass() + " for property " + 
	            propertyDescriptor.getDisplayName() + " because " +  e.getMessage());
	    }
	}
	
	/**
	 * 利用反射，复制两个bean的属性
	 * 
	 * <p>Spring/Apache也提供了类似的工具，但还是考虑自己实现一个更灵活的轮子
	 */
	public static final BeanCopyPropertiesHandler copyProperties(Object sourceBean, Object targetBean) {
	    return new BeanCopyPropertiesHandler(sourceBean, targetBean);
	}
	
	@Data
	public static class BeanCopyPropertiesHandler {
	    private Object sourceBean;
	    private Object targetBean;
	    
	    public BeanCopyPropertiesHandler(Object sourceBean, Object targetBean) {
	        this.sourceBean = sourceBean;
	        this.targetBean = targetBean;
	    }
	    
	    /**
	     * 复制属性
	     */
        public <T, U> BeanCopyPropertiesHandler copy(String sourceProp, String destProp, Function<T, U> converter) {
            PropertyDescriptor sourcePropDescriptor;
            PropertyDescriptor targetPropDescriptor;
            try {
                sourcePropDescriptor = new PropertyDescriptor(sourceProp, sourceBean.getClass(), 
                        "is" + capitalize(sourceProp),// 根据源码：这里若用is前缀，则当找不到该方法时还会尝试获取一次get前缀的方法 ovO
                        null);
                targetPropDescriptor = new PropertyDescriptor(destProp, targetBean.getClass(), null, 
                        "set" + capitalize(destProp));
            } catch (IntrospectionException e) {
                throw new WrapperException(e);
            }

            // 获取属性
            Object value = invokeGetter(sourcePropDescriptor, sourceBean);

            // 转换属性
            if (converter != null) {
                value = converter.apply((T) value);
            }

            // 设置属性
            invokeSetter(targetPropDescriptor, targetBean, value);

            return this;
        }
        
        public static String capitalize(String name) {
            if (name == null || name.length() == 0) {
                return name;
            }
            return name.substring(0, 1).toUpperCase(ENGLISH) + name.substring(1);
        }
	    
	    /**
	     * 复制属性，两个bean的属性类型必须一致
	     */
	    public BeanCopyPropertiesHandler copy(String prop) {
	        return this.copy(prop, prop, null);
	    }
	    
	    /**
	     * 复制属性，两个bean的属性类型必须一致
	     */
	    public BeanCopyPropertiesHandler copy(String sourceProp, String destProp) {
	        return this.copy(sourceProp, destProp, null);
	    }
	}
	
	/**
	 * 将bean的所有属性设为null（忽视基础数据类型），便于垃圾回收
	 */
	public static void cleanup(Object bean) {
	    List<PropertyDescriptor> propertyDescriptors = getPropertyDescriptor(bean);
	    for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
	        // 跳过基础数据类型的属性
	        if (propertyDescriptor.getPropertyType().isPrimitive()) {
	            continue;
	        }
            invokeSetter(propertyDescriptor, bean, null);
        }
	}
}
