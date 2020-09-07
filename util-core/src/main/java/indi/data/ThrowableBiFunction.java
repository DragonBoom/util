package indi.data;

@FunctionalInterface
public interface ThrowableBiFunction<T, K, R> {

    R apply(T t, K k) throws Throwable;
}
