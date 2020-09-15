package indi.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 本类用于为无法修改值的类型，提供间接修改值的封装；主要用于变量不能修改的场景，如函数内外传值
 * 
 * <p>除了封装外，还提供了一些边界的操作方法；由于过于冗杂，故汇总在一个类
 * 
 * @since 2020.09.07
 */
public class Wrapper {
	
    @Data
    @AllArgsConstructor(staticName = "of")
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
	
    @Data
    @AllArgsConstructor(staticName = "of")
	public static class DoubleWrapper{
	    private double value;
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
	
	@Data
	@AllArgsConstructor(staticName = "of")
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
	
	@Data
	@AllArgsConstructor(staticName = "of")
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
	
    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor(staticName = "of")
    public static class BooleanWrapper {
        private boolean value;
        
        public void setTrue() {
            this.value = true;
        }
        public void setFalse() {
            this.value = false;
        }
        public boolean get() {
            return this.value;
        }
    }
	
	@Data
	@AllArgsConstructor(staticName = "of")
	public static class ObjectWrapper<T> {
	    private T value;
	}
}
