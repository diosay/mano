/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package com.diosay.mano.io;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class Listener {

    final List<Channel> workers = new ArrayList<>();
    private AbstractChannelGroup server;
    private AsynchronousServerSocketChannel channel;
    private boolean started;
    private boolean stopped;

    /**
     * 初始化监听器。
     *
     * @param group
     */
    void init(AbstractChannelGroup group) {
        this.server = group;
    }

    public AbstractChannelGroup getGroup(){
        return server;
    }
    
    /**
     * 获取当前监听器的连接数。
     *
     * @return
     */
    public int size() {
        return workers.size();
    }

    /**
     * 绑定一个本地端口。
     */
    public synchronized Listener init(AsynchronousChannelGroup group, SocketAddress local, int backlog) throws IOException {
        if (channel == null) {
            //
        }
        channel = AsynchronousServerSocketChannel.open(group);
        channel.bind(local, backlog);
        channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        return this;
    }

    /**
     * 启动监听。
     */
    public final synchronized void run() {
        if (started) {
            return;
        } else if (channel == null) {
            throw new java.lang.IllegalStateException("监听器未初始化");
        }
        doStart();
    }
    
    protected Channel create(AsynchronousSocketChannel remote){
        return null;
    }

    /**
     * 开始监听。
     */
    protected void doStart() {
        channel.accept(channel, new CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel>() {

            @Override
            public void completed(AsynchronousSocketChannel remote, AsynchronousServerSocketChannel local) {
                
                System.out.println("try");
                synchronized (server.channelCount) {
                    if (server.channelCount.get() > 1024) {
                        try {
                            
                            server.channelCount.wait();
                        } catch (InterruptedException ex) {
                            //error and stop
                            try {
                                remote.close();
                            } catch (IOException ignored) {
                                //nothing
                            }
                            try {
                                failed(ex, local);
                            } catch (Exception ignored) {
                                //nothing
                            }
                            return;
                        }
                    }
                }
                server.add(create(remote));
                if (!stopped && started) {
                    System.out.println("c...");
                    local.accept(local, this);
                }
            }

            @Override
            public void failed(Throwable exc, AsynchronousServerSocketChannel local) {

            }
        });
        started = true;
    }
    
    

}
