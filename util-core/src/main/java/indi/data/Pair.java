package indi.data;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Pair<T, K> {
    private T first;
    private K second;
    
    public static final <T, K> Pair<T, K> of(T first, K second) {
        return new Pair<>(first, second);
    }
}
