package indi.obj;

public interface Cleanupable {

	/**
	 * 清空（而不是回收）当前实例，移除其绑定的引用，便于垃圾回收
	 */
	void cleanup();
}
