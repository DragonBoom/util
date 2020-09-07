package indi.expression.source;

import java.util.Map;

import lombok.Data;

/**
 * 描述了properties文件的对象，不包含注释
 * 
 * @author DragonBoom
 * @since 2019.10.26
 */
@Data
public class Properties {
    private Map<String, String> content;// 直接从配置文件获取的内容
}
