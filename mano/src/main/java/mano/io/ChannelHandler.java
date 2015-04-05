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
 *
 * @author sixmoon
 */
public interface ChannelHandler {

    void setProperty(String property,Object value);
    
    void init() throws Exception;
    
    void destroy();
    
    /**
     * 当通道建立连接并打开通道时调用。
     * <p>为防止意外发生，所有主动I/O操作都应该在该事件发生后操作。
     * @param context {@link ChannelContext}
     */
    void handleConnected(ChannelContext context,ChannelHandlerChain chain);

    /**
     * 当通道关闭后调用。关闭可能是主动也可能是被动的(如：网络连接意外断开)。
     * @param context {@link ChannelContext}
     */
    void handleDisconnected(ChannelContext context,ChannelHandlerChain chain);

    /**
     * 当从通道中读取到数据时调用。
     * @param context
     * @param buffer 
     */
    void handleInbound(ChannelContext context,ChannelHandlerChain chain, ByteBuffer buffer);
    
    /**
     * 当准备将数据写入通道时调用。
     * @param context
     * @param buffer 
     */
    void handleOutbound(ChannelContext context,ChannelHandlerChain chain, ByteBuffer buffer);
    
    /**
     * 当发生错误时调用。
     * @param context
     * @param cause 
     */
    void handleError(ChannelContext context,ChannelHandlerChain chain, Throwable cause);
}
