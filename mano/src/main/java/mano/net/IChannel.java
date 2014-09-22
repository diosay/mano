/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.net;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 表示一个网络通道。
 * @author jun <jun@diosay.com>
 */
public interface IChannel extends java.nio.channels.Channel{
    /**
     * 将一个消息放入待处理队列。
     * @param message 
     */
    void enqueue(Message message);
    
    /**
     * 从通道中读取数据并写入缓冲区。
     * @param <T> 附件类型。
     * @param buffer 缓冲区
     * @param attachment 附件
     */
    <T> void read(ByteBuffer buffer,T attachment) throws IOException;
    
    /**
     * 将缓冲区中的数据写入通道。
     * @param <T> 附件类型。
     * @param buffer 缓冲区。
     * @param attachment 附件。
     */
    <T> void write(ByteBuffer buffer,T attachment) throws IOException;
    
    /**
     * 将指定文件写入通道。
     * @param <T>
     * @param filename 文件名全路径。
     * @param position 偏移
     * @param length 长度.
     * @param attachment 附件。
     */
    <T> void write(String filename,long position,long length,T attachment) throws IOException;
    
    /**
     * 清空消息队列。
     */
    void clear();
    
    
}
