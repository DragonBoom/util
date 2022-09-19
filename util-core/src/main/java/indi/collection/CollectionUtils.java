package indi.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * 仅补充一些在现有工具类中找不到实现的方法。
 * 
 * <p>优先考虑使用java.util.Collections
 * 
 * @author DragonBoom
 *
 */
public class CollectionUtils {
    
    public static <T> List<T> clone(Collection<T> collection) {
        List<T> list = createList(collection.size());
        list.addAll(collection);
        return list;
    }
    
    /**
     * 轻度的，无引用地获取子列表。
     * 
     * <p>注意，List.subList(int, int)获取的子列表被原列表引用着的。
     * 
     * @param <T>
     * @param collection
     * @param begin 开始下标，是闭边界，包含当前元素 low endpoint (inclusive) 
     * @param end 结束下标，是开边界，不包含当前元素 high endpoint (exclusive)
     * @return
     */
    public static <T> List<T> cloneSubList(Collection<T> collection, int begin, int end) {
        int size = end - begin + 1;
        if (size < 0) {
            throw new IllegalArgumentException("pureSubList error: begin < end");
        }
        if (collection.size() <= end) {
            throw new IllegalArgumentException("pureSubList error: collection.size() < end");
        }
        List<T> list = createList(size);
        int index = 0;
        for (T t : collection) {
            if (index >= begin && index < end) {
                list.add(t);
            }
            index++;
            if (index == end) {
                break;
            }
        }
        return list;
    }
    
    private static final <T> List<T> createList(int size) {
        return new ArrayList<>(size);
    }

    private static final <T> Set<T> createSet(int size) {
        return new HashSet<>(size);
    }
    
    private static final <T, K> Map<T, K> createMap() {
        return new HashMap<>();
    }
    
    /**
     * 将两个数组转化为Map
     */
    public static <T, K> Map<T, K> collectMap(T[] a1, K[] a2, BiFunction<T, K, Boolean> fun) {
        return collectMap(ImmutableSet.copyOf(a1), ImmutableSet.copyOf(a2), fun);
    }
    
    /**
     * 将两个集合转化为Map
     */
    public static <T, K> Map<T, K> collectMap(ImmutableSet<T> set1, ImmutableSet<K> set2, BiFunction<T, K, Boolean> fun) {
        Map<T, K> result = createMap();
        int[] records = new int[set2.size()];
        for (T t : set1) {
            int i = 0;
            for (K k : set2) {
                if (records[i] != 1 && fun.apply(t, k)) {
                    records[i] = 1;// mark resolved
                    result.put(t, k);
                }
                i++;
            }
        }

        return result;
    }
    
    /**
     * 通过给定比较方式找出两集合间相同的元素；只返回来自第一个集合的元素；若第一个集合中有多个元素符合条件，均会返回；
     * 返回集合中元素顺序与第一个集合相同
     * 
     * @param comparatorFun nullable
     * @return 来自第一个集合
     */
    public static <T> List<T> listSameElements(Collection<T> c1, Collection<T> c2, @Nullable Comparator<T> comparator) {
        return listSameElements0(c1, c2, comparator);
    }
    
    /**
     * 通过给定比较方式找出两集合间相同的元素；只返回来自第一个集合的元素；若第一个集合中有多个元素符合条件，均会返回；
     * 返回集合中元素顺序与第一个集合相同
     * 
     * @param comparatorFun nullable
     * @return 来自第一个集合
     */
    public static <T> List<T> listSameElements(T[] a1, T[] a2, @Nullable Comparator<T> comparatorFun) {
        return listSameElements0(ImmutableSet.copyOf(a1), ImmutableSet.copyOf(a2), comparatorFun);
    }
    
    private static <T> List<T> listSameElements0(Collection<T> set1, Collection<T> set2, Comparator<T> comparator) {
        Set<T> result = createSet(Math.min(set1.size(), set2.size()));
        // 简单地用for-for循环实现
        for (T s1 : set1) {
            for (T s2 : set2) {
                if (comparator != null) {
                    if (comparator.compare(s1, s2) == 0) {
                        result.add(s1);
                    }
                } else if (s1 == s2 || (s1 != null && s1.equals(s2))) {
                    result.add(s1);
                }
            }
        }
        return Lists.newArrayList(result);
    }
    
    public static <T> boolean haveSameElements(Collection<T> set1, Collection<T> set2, @Nullable Comparator<T> comparator) {
        // 简单地用for-for循环实现
//        return set1.stream().anyMatch(set2::contains);
        for (T s1 : set1) {
            for (T s2 : set2) {
                if (comparator != null) {
                    if (comparator.compare(s1, s2) == 0) {
                        return true;
                    }
                } else if (s1 == s2 || (s1 != null && s1.equals(s2))) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 当且仅当集合只有一个元素时，返回该元素，<b>否则抛IllegalArgumentException</b>
     * 
     * @param list
     * @author DragonBoom
     * @since 2020.09.09
     */
    public static <T> T getSingle(List<T> list) {
        int size = list.size();
        if (size != 1) {
            throw new IllegalArgumentException("集合不是只有一个元素, size=" + size);
        }
        return list.get(0);
    }
    
    /** 判断给定Map的元素是否均为空（有null元素的不视为空） */
    public static boolean isEmpty(Map<?, ?>... maps) {
        for (Map<?, ?> map : maps) {
            if (map != null && !map.isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    /** 判断给定集合的元素是否均为空（有null元素的不视为空） */
    public static boolean isEmpty(Collection<?>... cs) {
        for (Collection<?> c : cs) {
            if (c != null && !c.isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    /** 判断给定数组是否均为空（有null元素的不视为空） */
    public static <T> boolean isEmpty(T[] f) {
        return f == null || f.length == 0;
    }
}
