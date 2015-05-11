/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.io;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;

/**
 * @deprecated 移除
 * @author sixmoon
 */
public interface ChannelHandlerOld {

    void setProperty(String property,Object value);
    
    void init() throws Exception;
    
    void destroy();
    
    /**
     * 当通道建立连接并打开通道时调用。
     * <p>为防止意外发生，所有主动I/O操作都应该在该事件发生后操作。
     * @param context {@link ChannelContextOld}
     */
    void handleConnected(ChannelContextOld context,ChannelHandlerChainOld chain);

    /**
     * 当通道关闭后调用。关闭可能是主动也可能是被动的(如：网络连接意外断开)。
     * @param context {@link ChannelContextOld}
     */
    void handleDisconnected(ChannelContextOld context,ChannelHandlerChainOld chain);

    /**
     * 当从通道中读取到数据时调用。
     * @param context
     * @param buffer 
     */
    void handleInbound(ChannelContextOld context,ChannelHandlerChainOld chain, ByteBuffer buffer);
    
    /**
     * 当准备将数据写入通道时调用。
     * @param context
     * @param buffer 
     */
    void handleOutbound(ChannelContextOld context,ChannelHandlerChainOld chain, ByteBuffer buffer);
    
    /**
     * 当发生错误时调用。
     * @param context
     * @param cause 
     */
    void handleError(ChannelContextOld context,ChannelHandlerChainOld chain, Throwable cause);
}
