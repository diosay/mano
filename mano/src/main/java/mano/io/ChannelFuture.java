/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.io;

/**
 *
 * @author jun
 */
public abstract class ChannelFuture {

    @FunctionalInterface
    public interface ChannelFutureTask {

        void execute(ChannelContext context, ChannelFuture future);
    }

    public final static int OP_READ=1;
    public final static int OP_WRITE=2;
    
    private final java.util.ArrayList<ChannelFutureTask> tasks = new java.util.ArrayList<>();
    private ChannelFutureProvider provider;
    private volatile long lifetime;
    private int op;
    public ChannelFuture(ChannelFutureProvider provider,int op) {
        //check
        if(op!=OP_READ && op!=OP_WRITE){
            throw new java.lang.IllegalArgumentException("op must be equals OP_READ or OP_WRITE.");
        }
        this.op=op;
        this.provider = provider;
    }

    public final int operation(){
        return op;
    }
    public final ChannelFutureProvider provider() {
        return this.provider;
    }
    
    /**
     * 获取上次刷新的时间。
     * @return 
     */
    public final long getLifetime(){
        return this.lifetime;
    }
    
    /**
     * 刷新时间用于计时操作。
     */
    public final ChannelFuture fresh(){
        this.lifetime=System.currentTimeMillis();
        return this;
    }

    public final synchronized void release() {
        //provider().release(this);
    }

    public final void execute(ChannelContext context) {
        doExecute(context);
        synchronized (tasks) {
            for (ChannelFutureTask task : tasks) {
                task.execute(context, this);
            }
        }
    }

    public final ChannelFuture addTask(ChannelFutureTask task) {
        if (task == null) {
            throw new NullPointerException("task");
        }
        synchronized (tasks) {
            tasks.add(task);
        }
        return this;
    }
    
    protected abstract void doExecute(ChannelContext context);

}
