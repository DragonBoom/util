package indi.thread;

/**
 * 为Thread接口额外实现一些方法
 * 
 * @author wzh
 * @since 2019.12.07
 */
public abstract class BasicThread extends Thread {
    /**
     * 启动为守护线程
     */
    public void startDeamon() {
        this.setDaemon(true);
        super.start();// 必须是super，用this可能会调用到子类的start方法！
    }
    
    /**
     * 启动为非守护线程
     */
    public void startNotDeamon() {
        this.setDaemon(false);
        super.start();// 必须是super，用this会调用到子类的start方法！
    }
    
    /*
     * 下列构造函数全都直接继承自Thread，没有任何修改
     */

    public BasicThread() {
        super();
        // TODO Auto-generated constructor stub
    }

    public BasicThread(Runnable target, String name) {
        super(target, name);
        // TODO Auto-generated constructor stub
    }

    public BasicThread(Runnable target) {
        super(target);
        // TODO Auto-generated constructor stub
    }

    public BasicThread(String name) {
        super(name);
        // TODO Auto-generated constructor stub
    }

    public BasicThread(ThreadGroup group, Runnable target, String name, long stackSize) {
        super(group, target, name, stackSize);
        // TODO Auto-generated constructor stub
    }

    public BasicThread(ThreadGroup group, Runnable target, String name) {
        super(group, target, name);
        // TODO Auto-generated constructor stub
    }

    public BasicThread(ThreadGroup group, Runnable target) {
        super(group, target);
        // TODO Auto-generated constructor stub
    }

    public BasicThread(ThreadGroup group, String name) {
        super(group, name);
        // TODO Auto-generated constructor stub
    }
}
