/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.io;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import mano.EventArgs;
import mano.EventHandler;
import mano.EventListener;
import mano.util.CachedObjectRecyler;
import mano.util.Pool;
import mano.util.ThreadPool;

/**
 * 用于支持
 * {@link Channel}、{@link ChannelAcceptor}和{@link ChannelConnector}工作的辅助类并管理相关资源。
 * <p>
 * 它也是配合{@link ChannelHandler}处理具体业务的组合流水线,{@link ChannelHandler}将按先入先调用的原则进行工作。
 *
 * @author sixmoon
 */
public class ChannelListenerContext {

    //只能使用这个，因为异步任务中会有步骤阻塞，一步阻塞后其它步骤也将阻塞，最后将所有线程耗尽
    private ExecutorService executor;
    private ByteBufferPool bufferPool;
    private EventListener.EventListenerHandle<ChannelListener, EventArgs> listenerClosedEventHandle;
    private Pool<ChannelTaskInternal> taskPool = new CachedObjectRecyler<ChannelTaskInternal>() {
        @Override
        protected ChannelTaskInternal createNew() {
            return new ChannelTaskInternal();
        }
    };
    private BufferManager bufferManager;

    public ChannelListenerContext(ExecutorService executor) {
        this.executor = executor;
        this.bufferPool = new ByteBufferPool(4096);
        listenerClosedEventHandle = EventListener.create();
        bufferManager=new BufferManager();
    }

    public Pool<ChannelTaskInternal> getTaskPool() {
        return taskPool;
    }

    public ChannelTask wrapWriteTask(ByteBuffer buffer) {
        ChannelTaskInternal task = taskPool.get();
        task.set(ChannelTaskInternal.OP_WRITE, buffer);
        return task;
    }

    public ChannelTask wrapReadTask(ByteBuffer buffer) {
        ChannelTaskInternal task = taskPool.get();
        task.set(ChannelTaskInternal.OP_READ, buffer);
        return task;
    }

    public boolean putTask(ChannelTask task) {
        if (task == null || !(task instanceof ChannelTaskInternal)) {
            return false;
        }
        return taskPool.put((ChannelTaskInternal) task);
    }

    public ByteBuffer getByteBuffer() {
        return bufferPool.get();
    }

    public boolean putByteBuffer(ByteBuffer buffer) {
        return bufferPool.put(buffer);
    }
    
    public BufferManager getBufferManager(){
        return this.bufferManager;
    }

    public ExecutorService getExecutor() {
        return this.executor;
    }

    public EventListener<EventHandler<ChannelListener, EventArgs>> listenerClosedEvent() {
        return listenerClosedEventHandle.getListener();
    }
    final java.util.ArrayList<ChannelListener> listeners = new java.util.ArrayList<>();
    EventHandler<ChannelListener, EventArgs> listenerClosedHandler = (sender, e) -> {
        synchronized (listeners) {
            if (!listeners.contains(sender)) {
                listeners.remove(sender);
                listenerClosedEventHandle.fire(sender, e);
            }
        }
    };

    public void addListener(ChannelListener listener) {
        synchronized (listeners) {
            if (!listeners.contains(listener)) {
                listener.closedEvent().add(listenerClosedHandler);
                listeners.add(listener);
            }
        }
    }
    
    public int size(){
        return listeners.size();
    }
    
    public Iterator<ChannelListener> getListeners(){
        return listeners.iterator();
    }

}
