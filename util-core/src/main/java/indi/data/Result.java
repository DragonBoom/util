package indi.data;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import indi.bean.ObjectMapperUtils;
import indi.exception.RuntimeException2;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 不要过度简化
 * 
 * @author wzh
 * @since 2019.12.19
 * @param <T>
 */
@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 97093511664589819L;
    private Integer code;
    private boolean error;
    private String msg;
    private T content;// success content or error content
    
    public T get() {
        return content;
    }

    @SuppressWarnings("unchecked")
    public <R> Result<R> then(Function<T, Result<R>> fun) {
        if (!isError()) {
            return fun.apply(content);
        } else {
            return (Result<R>) this;
        }
    }
    
    public Result<T> toError(boolean isError) {
        return isError ? Results.error() : this;
    }

    public Result<T> toError(boolean isError, T errorContent) {
        return isError ? Results.error(errorContent) : this;
    }

    public Result<?> throwIfError() {
        return throwIfError(null);
    }

    public Result<?> throwIfError(String msg) {
        if (isError()) {
            throw new RuntimeException2(Optional.ofNullable(msg).orElse(this.msg));
        }
        return this;
    }

    /**
     * 映射 x->y OvO
     */
    @SuppressWarnings("unchecked")
    public <R> Result<R> map(Function<T, R> function) {
        if (!isError()) {
            return Results.success(function.apply(content));
        } else {
            return (Result<R>) this;
        }
    }

    public Result<T> run(Consumer<T> function) {
        if (!isError()) {
            function.accept(content);
        }
        return this;
    }

    /**
     * eat content。类似于map，但返回的是Result的内容，而不是Result
     */
    public <R> R eat(Function<T, R> function) {
        return function.apply(content);
    }

    public <R> R eatMsg(Function<String, R> function) {
        return function.apply(msg);
    }

    /**
     * 该类剩余方法由Lombok补充
     * 
     * @author wzh
     * @since 2020.09.15
     * @param <T>
     */
    public static class ResultBuilder<T> {

        public ResultBuilder<T> error() {
            error = true;
            return this;
        }
    }

    /*
     * to json
     */

    public String toJson() throws JsonProcessingException {
        ObjectMapper objectMapper = ObjectMapperUtils.getMapper();
        return objectMapper.writeValueAsString(this);
    }
}
