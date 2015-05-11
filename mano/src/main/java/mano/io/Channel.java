/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.io;

import java.nio.ByteBuffer;
import java.nio.charset.CharsetEncoder;

/**
 * 表示一个支持异步 I/O 操作的信道。
 *
 * @author sixmoon
 */
public interface Channel extends java.nio.channels.Channel{

    /**
     * 提交任务到执行队列中。
     *
     * @param future {@link ChannelFuture}
     * @throws ChannelException 当前通道已经关闭时触发。
     */
    void submit(ChannelFuture future) throws ChannelException;

    /**
     * 判断此信道是否使用安全套接字（即 SSL/TLS）。
     */
    boolean isSecure();

    /**
     * 判断通道中是否有挂起的写任务。
     * @deprecated 
     */
    //boolean hasPaddingWrite();

    /**
     * 判断通道中是否有挂起的读任务。
     * @deprecated f
     */
    //boolean hasPaddingRead();

    /**
     * 提交一个从信道中读取数据的任务到队列中。
     * @throws ChannelException 当前通道已经关闭时触发。
     */
    void read() throws ChannelException;
    
    /**
     * 提交一个从信道中读取数据的任务到队列中。
     * @throws ChannelException 当前通道已经关闭时触发。
     */
    void read(ByteBuffer buffer) throws ChannelException;

    /**
     * 将缓冲区排入队列以便执行读操作。
     * <p>
     * 注：该方法请在具体任务中实现，不要在具体业务代码中调用。
     * 参见 {@link ChannelTask}。
     * 
     * @deprecated 
     * @param buffer {@link ByteBuffer}
     * @throws ChannelException 当前通道已经关闭时触发。
     */
    //void queueReadBuffer(ByteBuffer buffer) throws ChannelException;

    /**
     * 将指定字符序列经过编码后写入信道。
     *
     * @param content 字符序列。
     * @param encoder 字符编码器，参见 {@link CharsetEncoder}。
     * @throws ChannelException 当前通道已经关闭时触发。
     */
    void write(CharSequence content, CharsetEncoder encoder) throws ChannelException;

    /**
     * 将指定字节组写入信道。
     *
     * @param buffer 字节组。
     * @throws ChannelException 当前通道已经关闭时触发。
     */
    void write(byte[] buffer) throws ChannelException;

    /**
     * 将指定字节组写入信道。
     *
     * @param buffer 字节组。
     * @throws ChannelException 当前通道已经关闭时触发。
     */
    void write(ByteBuffer buffer,boolean copy) throws ChannelException;
    
    /**
     * 将指定字节组写入信道。
     *
     * @param buffer 字节组。
     * @param offset 字节组的开始位置。
     * @param count 要写入的长度。
     * @throws ChannelException 当前通道已经关闭时触发。
     */
    void write(byte[] buffer, int offset, int count) throws ChannelException;

    void write(String filename, long position, long length) throws ChannelException;
    
    /**
     * @deprecated 
     * 将缓冲区排入队列以便执行写操作。
     * <p>
     * 注：该方法请在具体任务中实现，不要在具体业务代码中调用。
     * 参见 {@link ChannelTask}。
     * @param buffer {@link ByteBuffer}
     * @throws ChannelException 当前通道已经关闭时触发。
     */
    //void queueWriteBuffer(ByteBuffer buffer) throws ChannelException;

    /**
     * 发起关闭当前数据通道。
     *
     * @param force {@code true} 表示立即关闭，否则将继续处理相应工作后关闭。
     */
    void close(boolean force);

    /**
     * 立即关闭当前通道，并触发一次 {@link ChannelHandler#handleDisconnected(mano.io.Channel) } 事件。
     */
    @Override
    void close();
    
    void await();
}
