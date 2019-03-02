package indi.util;

/**
 * 类似于toString<br>
 * toString方法每个类都有，无法判断对象是否自己实现了toString，所以用getMessage代替，用getMessage方法一定能获得对象自己生成的字符串<br>
 * 该接口也可用于对象除了toString外，还需要另外生成字符串的情况
 * 
 * @author DragonBoom
 *
 */
public interface Message {

	String getMessage();
}
