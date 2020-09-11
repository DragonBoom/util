package indi.data;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 本类用于为无法修改值的类型，提供间接修改值的封装；主要用于变量不能修改的场景，如函数内外传值
 * 
 * <p>除了封装外，还提供了一些边界的操作方法；由于过于冗杂，故汇总在一个类
 * 
 * @since 2020.09.07
 */
public class Wrapper {
	
    @Getter
    @Setter
    @ToString
    @Builder
	public static class FloatWrapper{
	    private float value;

	    //加法
	    public void plus(float num) {
	        this.value += num;
	    }
	    //减法
	    public void minus(float num) {
	        this.value -= num;
	    }
	    //乘法
	    public void times(float num) {
	        this.value *= num;
	    }
	    //除法
	    public void divide(float num) {
	        this.value /= num;
	    }
	}
	
	@Getter
	@Setter
    @ToString
    @Builder
	public static class DoubleWrapper{
	    private Double value;
	    //加法
	    public void plus(double num) {
	        this.value += num;
	    }
	    //减法
	    public void minus(double num) {
	        this.value -= num;
	    }
	    //乘法
	    public void times(double num) {
	        this.value *= num;
	    }
	    //除法
	    public void divide(double num) {
	        this.value /= num;
	    }
	}
	
	@Getter
	@Setter
	@ToString
	@Builder
	public static class IntWrapper {
	    private int value;
	    //加法
	    public void plus(int num) {
	        this.value += num;
	    }
	    //加1
	    public void plus() {
	        this.value += 1;
	    }
	    //减法
	    public void minus(int num) {
	        this.value -= num;
	    }
	    //减一
	    public void minus() {
	        this.value -= 1;
	    }
	    //乘法
	    public void times(int num) {
	        this.value *= num;
	    }
	    //除法
	    public void divide(int num) {
	        this.value /= num;
	    }
	}
	
	@Getter
	@Setter
    @ToString
    @Builder
	public static class LongWrapper{
	    private long value;
	    
	    //加法
	    public void plus(long num) {
	        this.value += num;
	    }
	    //加1
	    public void plus() {
	        this.value += 1;
	    }
	    //减法
	    public void minus(long num) {
	        this.value -= num;
	    }
	    //减一
	    public void minus() {
	        this.value -= 1;
	    }
	    //乘法
	    public void times(long num) {
	        this.value *= num;
	    }
	    //除法
	    public void divide(long num) {
	        this.value /= num;
	    }
	}
	
	@Getter
	@Setter
	@ToString
	@Builder
	public static class ObjectWrapper<T> {
	    private T value;
	}
}
