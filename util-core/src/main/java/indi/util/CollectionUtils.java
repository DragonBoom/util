package indi.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CollectionUtils {
    
    public static <T> List<T> clone(Collection<T> collection) {
        List<T> list = createList(collection.size());
        list.addAll(collection);
        return list;
    }
    
    /**
     * 轻度的，无引用地获取子列表
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
}
