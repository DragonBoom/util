package indi.data;

public interface Result<T>{
    T getContent();
    boolean isError();
    boolean isSuccess();
}
