/**
 * 
 */
package indi.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author wzh
 * @since 2020.09.04
 */
public class Wrapper {

    @Getter
    @Setter
    @ToString
    @Builder
    public static class ObjectWrapper {
        private Object value;
    }
    
    @Getter
    @Setter
    @ToString
    @Builder
    public static class BooleanWrapper {
        private Boolean value;
    }
}
