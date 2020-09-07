package indi.data;

/**
 * 能对外抛出异常的Function
 * 
 * @author DragonBoom
 *
 * @param <T>
 * @param <R>
 */
@FunctionalInterface
public interface ThrowableFunction<T, R> {

    R apply(T t) throws Throwable;
}
