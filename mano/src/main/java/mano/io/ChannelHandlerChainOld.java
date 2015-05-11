/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.io;

import java.nio.ByteBuffer;

/**
 * @deprecated 移除
 * 用于组织{@link ChannelHandler}的链表。
 * @author sixmoon
 */
public interface ChannelHandlerChainOld extends java.lang.AutoCloseable {
    /**
     * 当通道建立连接并打开通道时调用。
     * <p>为防止意外发生，所有主动I/O操作都应该在该事件发生后操作。
     * @param context {@link ChannelContextOld}
     */
    void handleConnected(ChannelContextOld context);

    /**
     * 当通道关闭时调用。关闭可能是主动也可能是被动的(如：网络连接意外断开)。
     * @param context {@link ChannelContextOld}
     */
    void handleDisconnect(ChannelContextOld context);

    /**
     * 当从通道中读取到数据时调用。
     * @param context
     * @param buffer 
     */
    void handleInbound(ChannelContextOld context, ByteBuffer buffer);
    
    /**
     * 当准备将数据写入通道时调用。
     * @param context
     * @param buffer 
     */
    void handleOutbound(ChannelContextOld context, ByteBuffer buffer);
    
    /**
     * 当发生错误时调用。
     * @param context
     * @param cause 
     */
    void handleError(ChannelContextOld context, Throwable cause);
    
    /**
     * 回收
     */
    @Override
    void close();
    
    /**
     * 复制一个新的实例。
     * @return 
     */
    ChannelHandlerChainOld duplicate();
}
