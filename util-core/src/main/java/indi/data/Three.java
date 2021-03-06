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
public class Three<T, R, K> {
    private T first;
    private R second;
    private K third;
    
    private Three() {}

    public static final <T, R, K> Three<T, R, K> of(T first, R second, K third) {
        return new Three<>(first, second, third);
    }
}
