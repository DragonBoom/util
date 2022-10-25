package indi.data;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import indi.bean.ObjectMapperUtils;

/**
 * Tool for Result。暂时觉得没必要与Result合在一起，分开可以提高代码可读性
 * 
 * @author DragonBoom
 *
 */
public class Results {

    /*
     * json <-> Result
     */

    private static final TypeReference<Result<String>> DEFAULT_TYPE_REFERENCE = new TypeReference<Result<String>>() {
    };

    public static Result<String> fromJson(String json) throws JsonParseException, JsonMappingException, IOException {
        return fromJson(json, DEFAULT_TYPE_REFERENCE);
    }

    public static <R> Result<R> fromJson(String json, TypeReference<Result<R>> typeRef) throws IOException {
        ObjectMapper objectMapper = ObjectMapperUtils.getMapper();
        return objectMapper.readValue(json, typeRef);
    }

    public static final ImmutableMap<Object, Object> commonTypeRef = ImmutableMap.builder()
            .put("MAP", new TypeReference<Result<Map<String, String>>>() {
            }).build();

    public enum CommonTypeRef {
        /**
         * Map<\String, String>
         */
        MAP("MAP");

        private String str;

        CommonTypeRef(String str) {
            this.str = str;
        }

        public String getStr() {
            return str;
        }
    }

    /**
     * 将json字符串转化为常见的Result类型<br>
     * support "MAP" -> Map<String, String>
     */
    public static <R> Result<R> fromCommonJson(String json, CommonTypeRef type) throws IOException {
        Objects.requireNonNull(type);
        TypeReference<?> typeRef = (TypeReference<?>) commonTypeRef.get(type.getStr());
        Object value = ObjectMapperUtils.getMapper().readValue(json, typeRef);
        Objects.requireNonNull(value, "不存在该类型引用");
        if (value instanceof Result) {
            return (Result<R>) value;
        } else {
            return Results.success((R) value);
        }
    }
    
    /*
     * Optional <-> Result
     */
    
    public static <T> Result<T> fromOptional(Optional<T> optional, String msg) {
        return optional.map(Results::success).orElse(Results.error(msg));
    }

    /*
     * 下面部分为用于创建新的实例的静态方法
     */

    /**
     * 本方法要不就返回success，不要就抛异常
     *
     * @param fun
     * @param <T>
     * @return
     */
    public static <T> Result<T> run(Runnable fun) {
        fun.run();
        return Results.success();
    }

    public static <T> Result<T> success() {
        return Result.<T>builder().build();
    }

    public static <T> Result<T> success(T content) {
        return Result.<T>builder().content(content).build();
    }

    public static <T> Result<T> success(Integer code, String msg, T content) {
        return Result.<T>builder().code(code).msg(msg).content(content).build();
    }

    public static <T> Result<T> error() {
        return Result.<T>builder().error().build();
    }

    public static <T> Result<T> error(String msg) {
        return Result.<T>builder().error().msg(msg).build();
    }

    public static <T> Result<T> error(T content) {
        return Result.<T>builder().error().content(content).build();
    }

    public static <T> Result<T> error(Integer code, T content) {
        return Result.<T>builder().error().code(code).content(content).build();
    }
    
    public static <T> Result<T> error(Integer code, String msg) {
        return Result.<T>builder().error().code(code).msg(msg).build();
    }

    public static <T> Result<T> error(Integer code, String msg, T content) {
        return Result.<T>builder().error().code(code).msg(msg).content(content).build();
    }
}
