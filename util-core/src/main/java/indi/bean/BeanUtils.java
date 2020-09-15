package indi.bean;

import static java.util.Locale.ENGLISH;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import indi.collection.CollectionUtils;
import indi.exception.WrapperException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BeanUtils {

	/**
	 * 将bean转化为新的Map<String, Object>。值为null的属性将被忽略。
	 */
	public static final Map<String, Object> createMap(Object bean) {
		return createMap(bean, true);
	}
	
	/** 读取、写入Bean的属性时需要跳过的属性名 */
	private static final String CLASS_PROPERTIE = "class";

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

			if (name.equals(CLASS_PROPERTIE)) {// 跳过class属性
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
		        .filter(propertyDescriptor -> !CLASS_PROPERTIE.equals(propertyDescriptor.getName()))
		        .collect(Collectors.toList());
		propertyDescriptors.removeIf(propertyDescriptor -> CLASS_PROPERTIE.equals(propertyDescriptor.getName()));
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
        } catch (Exception e) {
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
	    } catch (Exception e) {
	        throw new WrapperException("Invoke setter error in " + bean.getClass() + " for property " + 
	            propertyDescriptor.getDisplayName() + " because " +  e.getMessage());
	    }
	}
	
	public static final void copyProperties(Object sourceBean, Object targetBean, String... ignoreProperties) {
	    try {
            BeanInfo sourceBeanInfo = Introspector.getBeanInfo(sourceBean.getClass());
            PropertyDescriptor[] sourcePDs = sourceBeanInfo.getPropertyDescriptors();

            BeanInfo targetBeanInfo = Introspector.getBeanInfo(sourceBean.getClass());
            PropertyDescriptor[] targetPDs = targetBeanInfo.getPropertyDescriptors();
            
            CollectionUtils
                    .collectMap(sourcePDs, targetPDs, (sourcePD, targetPD) -> {
                        String name = sourcePD.getName();
                        return Arrays.binarySearch(ignoreProperties, name) >= 0 && name.equals(targetPD.getName());
                    })
                    .forEach((sourcePD, targetPD) -> {
                        Object value = invokeGetter(sourcePD, sourceBean);
                        invokeSetter(targetPD, targetBean, value);
                    });
        } catch (IntrospectionException e) {
            throw new WrapperException(e);
        }
	}
	
	/**
	 * 获取包含了getter与setter方法的PropertyDescriptor
	 * 
	 * @param classes
	 * @param propertyName
	 * @return
	 */
	public static PropertyDescriptor getFullPropertyDescriptor(Class<?> classes, String propertyName) {
	    try {
            return new PropertyDescriptor(propertyName, classes, 
                    "is" + capitalize(propertyName),// 根据源码：这里若用is前缀，则当找不到该方法时还会尝试获取一次get前缀的方法 ovO
                    "set" +  capitalize(propertyName));
        } catch (IntrospectionException e) {
            throw new WrapperException(e);
        }
	}
	
	/**
	 * 首字母大写，用于拼接方法名
	 * 
	 * @param name bean的属性
	 */
	private static String capitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        return name.substring(0, 1).toUpperCase(ENGLISH) + name.substring(1);
    }
	
	/**
	 * 利用反射，复制两个bean的属性
	 * 
	 * <p>Spring/Apache也提供了类似的工具，但还是考虑自己实现一个更灵活的轮子
	 */
	public static final <T, K> CopyPropertyBuilder<T, K> copySelectedProperties(T sourceBean, K targetBean) {
	    return new CopyPropertyBuilder<>(sourceBean, targetBean);
	}
	
	@AllArgsConstructor
	public static class CopyPropertyBuilder<T, K> {
	    private T sourceBean;
	    private K targetBean;
	    
	    /**
	     * 复制属性
	     * 
	     * @param converter 源属性的转换函数（不论源属性是否为空都会执行）
	     * @param sourcePredicate 校验是否需要复制源属性的函数，将在转换之后执行
	     */
        public CopyPropertyBuilder<T, K> copy(String sourceProp, String destProp, UnaryOperator<Object> converter, 
                Predicate<Object> sourcePredicate) {
            PropertyDescriptor sourcePropDescriptor;
            PropertyDescriptor targetPropDescriptor;
            try {
                // build getter propertyDescriptor
                sourcePropDescriptor = new PropertyDescriptor(sourceProp, sourceBean.getClass(), 
                        "is" + capitalize(sourceProp),// 根据源码：这里若用is前缀，则当找不到该方法时还会尝试获取一次get前缀的方法 ovO
                        null);

                // build setter propertyDescriptor
                targetPropDescriptor = new PropertyDescriptor(destProp, targetBean.getClass(), null, 
                        "set" + capitalize(destProp));
            } catch (IntrospectionException e) {
                throw new WrapperException(e);
            }

            // 获取源属性
            Object value = invokeGetter(sourcePropDescriptor, sourceBean);
            
            // 转换源属性
            if (converter != null) {
                value = converter.apply(value);
            }
            
            // 校验是否需要复制
            if (sourcePredicate != null && !sourcePredicate.test(value)) {
                // 校验不通过，不复制
                return this;
            }

            // 设置到目标属性
            invokeSetter(targetPropDescriptor, targetBean, value);

            return this;
        }
	    
	    /**
	     * 复制属性，两个bean的属性类型必须一致
	     */
	    public CopyPropertyBuilder<T, K> copy(String prop) {
	        return this.copy(prop, prop, null, null);
	    }
	    
	    /**
	     * 只复制不为空的源属性
	     * 
	     * @param prop
	     * @param sourcePredicate
	     * @return
	     * @author DragonBoom
	     * @since 2020.09.09
	     */
	    public CopyPropertyBuilder<T, K> copyExist(String prop) {
	        return this.copy(prop, prop, null, Objects::nonNull);
	    }
	    
	    /**
	     * 复制属性，两个bean的属性类型必须一致
	     */
	    public CopyPropertyBuilder<T, K> copy(String sourceProp, String destProp) {
	        return this.copy(sourceProp, destProp, null, null);
	    }

	}
	
	public static SetPropertyBuilder setProperties(Object bean) {
	    return new SetPropertyBuilder(bean);
	}
	
	@AllArgsConstructor
    public static final class SetPropertyBuilder {
	    private Object bean;
	    
	    public SetPropertyBuilder set(String propertyName, Object propertyValue) {
	        PropertyDescriptor propertyDescriptor = getFullPropertyDescriptor(bean.getClass(), propertyName);

	        invokeSetter(propertyDescriptor, bean, propertyValue);

	        return this;
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
	
	/**
	 * 性能可能较差
	 * 
	 * @param obj
	 * @param propertyName
	 * @return
	 * @author DragonBoom
	 * @since 2020.09.04
	 */
	public static <T> T getProperty(Object obj, String propertyName) {
	    PropertyDescriptor propertyDescriptor = BeanUtils.getFullPropertyDescriptor(obj.getClass(), propertyName);
	    return (T) BeanUtils.invokeGetter(propertyDescriptor, obj);
	}
	
	/**
	 * 性能可能较差
	 * 
	 * @param obj
	 * @param propertyName
	 * @param value
	 * @return
	 * @author DragonBoom
	 * @since 2020.09.04
	 */
	public static void setProperty(Object obj, String propertyName, Object value) {
	    PropertyDescriptor propertyDescriptor = BeanUtils.getFullPropertyDescriptor(obj.getClass(), propertyName);
	    BeanUtils.invokeSetter(propertyDescriptor, obj, value);
	}
	
	
}
