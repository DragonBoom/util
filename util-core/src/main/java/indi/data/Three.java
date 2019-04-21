package indi.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@Getter
@Setter
public class Three {
    private Object first;
    private Object second;
    private Object third;
    
    private Three() {}

    public static final Three of(Object first, Object second, Object third) {
        Three three = new Three();
        three.first = first;
        three.second = second;
        three.third = third;
        return three;
    }
}
