package indi.directory;

import java.nio.file.AccessDeniedException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;

/**
 * 常见异常字典
 * 
 * @author DragonBoom
 *
 */
public class CommonExceptionDictionary {

    public CommonExceptionDictionary() {
    }

    private static final ImmutableMap<Class<? extends Throwable>, Function<Throwable, String>> translatorMap = 
        ImmutableMap
            .<Class<? extends Throwable>, Function<Throwable, String>>builder()
            // 0.
            .put(AccessDeniedException.class, e -> {
                
                return "无法访问文件";
            })
            // 1.
            .put(NullPointerException.class, e -> {
                StringBuilder sb = new StringBuilder("空指针异常：");
                // read stack traces directly
                StackTraceElement[] stackTraces = e.getStackTrace();
                sb.append(Arrays.toString(stackTraces));
                // over
                return sb.toString();
            })
            .build();
    
    /**
     * 根据字典现有数据翻译异常，若无法翻译则返回null
     */
    public static String translate(Throwable e) {
        Objects.requireNonNull(e);
        return Optional.ofNullable(translatorMap.get(e.getClass()))
                .map(translator -> translator.apply(e))
                .orElse(null);
    }
}
