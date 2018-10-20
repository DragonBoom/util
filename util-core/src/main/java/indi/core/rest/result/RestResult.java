package indi.core.rest.result;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import indi.core.exception.RuntimeException2;
import indi.core.util.ObjectMapperUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * resultful 风格的类，封装普通的返回结果作为resultful接口的返回值
 * 
 * @author DragonBoom
 *
 */
@Getter
@Setter
@ToString
public class RestResult<T> implements Serializable {
    private static final long serialVersionUID = 97093511664589889L;
    private int code;
    private boolean error;
    private String msg;
    private T content;

    public String toJson() throws JsonProcessingException {
        ObjectMapper objectMapper = ObjectMapperUtils.getMapper();
        return objectMapper.writeValueAsString(this);
    }

    private static final TypeReference<RestResult<String>> DEFAULT_TYPE_REFERENCE = 
            new TypeReference<RestResult<String>>() {};

    public static RestResult<String> fromJson(String json)
            throws JsonParseException, JsonMappingException, IOException {
        return fromJson(json, DEFAULT_TYPE_REFERENCE);
    }

    public static <R> RestResult<R> fromJson(String json, TypeReference<RestResult<R>> typeRef)
            throws JsonParseException, JsonMappingException, IOException {
        // cache type ref
        ObjectMapper objectMapper = ObjectMapperUtils.getMapper();
        return objectMapper.readValue(json, typeRef);
    }
    
    

    public static final ImmutableMap<Object, Object> commonTypeRef = ImmutableMap.builder()
            .put("MAP", new TypeReference<RestResult<Map<String, String>>>() {})
            .build();

    public static enum CommonTypeRef {
        /**
         * Map<\String, String>
         */
        Map("MAP");

        private String str;

        CommonTypeRef(String str) {
            this.str = str;
        }

        public String getStr() {
            return str;
        }
    }

    /**
     * 将json字符串转化为常见的RestResult类型<br>
     * support "MAP" -> Map<String, String>
     */
    public static RestResult<?> fromCommonJson(String json, CommonTypeRef type) throws JsonParseException, JsonMappingException, IOException {
        Objects.requireNonNull(type);
        TypeReference<?> typeRef = (TypeReference<?>) commonTypeRef.get(type.getStr());
        Object value = ObjectMapperUtils.getMapper().readValue(json, typeRef);
        Objects.requireNonNull(value, "不存在该类型引用");
        if (value instanceof RestResult) {
            return (RestResult<?>) value;
        } else {
            return RestResult.asSuccess(value);
        }
    }

    private static <T> RestResult<T> asSuccessOrError(final boolean error, T content, String msg,
            Integer code) {
        RestResult<T> result = new RestResult<T>();
        result.setContent(content);
        result.setError(error);
        result.setMsg(Optional.ofNullable(msg).orElse(""));
        result.setCode(Optional.ofNullable(code).orElseGet(() -> {
            if (!error) {
                return 200;
            }
            return 400;
        }));
        return result;
    }

    public RestResult<?> throwIfError() {
        return throwIfError(null);
    }

    public RestResult<?> throwIfError(String msg) {
        if (error) {
            throw new RuntimeException2(Optional.ofNullable(msg).orElse(this.msg));
        }
        return this;
    }

    /**
     * 映射 x->y OvO
     */
    public <R> RestResult<?> map(Function<T, R> function) {
        if (!error) {
            return RestResult.asSuccess(function.apply(content));
        }
        return this;
    }
    
    public RestResult<T> run(Consumer<T> function) {
        if (!isError()) {
            function.accept(content);
        }
        return this;
    }

    /**
     * eat content
     */
    public <R> R eat(Function<Object, R> function) {
        return function.apply(content);
    }

    public <R> R eatMsg(Function<Object, R> function) {
        return function.apply(msg);
    }

    public static <T> RestResult<T> asSuccess(Integer code, String msg, T content) {
        return asSuccessOrError(false, content, msg, code);
    }

    public static <T> RestResult<T> asSuccess(T content) {
        return asSuccess(null, null, content);
    }

    public static final RestResult<Object> SUCCESS_RESULT = RestResult.asSuccess(null);

    public static RestResult<?> asError(Integer code, String msg, Object content) {
        return asSuccessOrError(true, content, msg, code);
    }

    public static RestResult<?> asError(Integer code) {
        return asError(code, null, null);
    }

    public static RestResult<?> asError(Integer code, String msg) {
        return asError(code, msg, null);
    }

    public static RestResult<?> asSuccess() {
        return asSuccess(null);
    }

}
