/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package com.diosay.mano.io;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 服务器网络启动程序。
 *
 * @author jun <jun@diosay.com>
 */
public abstract class AbstractChannelGroup {

    private final List<Listener> listeners = new ArrayList<>();
    final AtomicInteger channelCount = new AtomicInteger(0);
    Class<? extends ChannelHanlder> hanlderType;
    ExecutorService service;

    public <T extends Listener> T regsister(T listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener");
        }
        listener.init(this);
        synchronized (listeners) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
        return listener;
    }

    /**
     * 注册一个通道。
     *
     * @param worker
     */
    protected void add(Channel worker) {
        if (worker == null) {
            return;
        }
        synchronized (channelCount) {
            if (!worker.getListener().workers.contains(worker)) {
                worker.getListener().workers.add(worker);
                channelCount.getAndIncrement();
            }else{
                return;
            }
        }

        try {
            ChannelHanlder handler = getHandler();
            handler.connected(worker);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * 注销一个通道。
     *
     * @param worker
     */
    protected void remove(Channel worker) {
        if (worker == null) {
            return;
        }
        synchronized (channelCount) {
            if (worker.getListener().workers.contains(worker)) {
                worker.getListener().workers.remove(worker);
                channelCount.getAndDecrement();
                channelCount.notify();
            }else{
                return;
            }
        }
        
        try {
            ChannelHanlder handler = getHandler();
            handler.closed(worker);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 开始监听
     */
    public void start() {
        if(hanlderType==null){
            throw new IllegalArgumentException("未设置处理程序。");
        }
        listeners.forEach((listener) -> {
            listener.run();
        });
    }

    /**
     * 获取线程执行服务。
     *
     * @return
     */
    public ExecutorService getExecutorService() {
        if (service == null) {
            service = Executors.newCachedThreadPool();
        }
        return service;
    }
    
    /**
     * 获取一个缓冲区。
     * @return 
     */
    public abstract ChannelBuffer allocate();
    
    /**
     * 释放一个缓冲区。
     * @param buffer 
     */
    public abstract void free(ChannelBuffer buffer);
    
    /**
     * 设置处理程序类型。
     * @param type 
     */
    public void setHandler(Class<? extends ChannelHanlder> type){
        hanlderType=type;
    }
    
    public ChannelHanlder getHandler(){
        try {
            return  hanlderType.newInstance();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    //http://blog.csdn.net/woshixuye/article/details/18862361
    //http11

}
