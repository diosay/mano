/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */

package com.diosay.mano.io;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 表示一个网络通道。
 * @author jun <jun@diosay.com>
 */
public interface Channel extends java.nio.channels.Channel{
    Listener getListener();
    /**
     * 将一个消息放入待处理队列。
     * @param message 
     */
    void enqueue(Message message);
    
    /**
     * 从通道中读取数据并写入缓冲区。
     * @param <T> 附件类型。
     * @param buffer 缓冲区
     * @param handler 处理程序。
     */
    <T extends Channel> void read(ChannelBuffer buffer,ChannelHanlder<T> handler) throws IOException;
    
    /**
     * 将缓冲区中的数据写入通道。
     * @param <T> 附件类型。
     * @param buffer 缓冲区。
     * @param attachment 附件。
     */
    <T extends Channel> void write(ChannelBuffer buffer,ChannelHanlder<T> handler) throws IOException;
    
    /**
     * 将指定文件写入通道。
     * @param <T>
     * @param filename 文件名全路径。
     * @param position 偏移
     * @param length 长度.
     * @param attachment 附件。
     */
    <T extends Channel> void write(String filename, long position, long length, ChannelHanlder<T> handler) throws IOException;
    
    /**
     * 清空消息队列。
     */
    void clear();
    
    
}
