/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.io;

import java.nio.ByteBuffer;

/**
 *
 * @author jun
 */
public interface ChannelHandlerChain extends java.lang.AutoCloseable{

    void handleOpened(ChannelContext context);

    void handleClosed(ChannelContext context);

    void handleInbound(ChannelContext context, ByteBuffer buffer);

    void handleOutbound(ChannelContext context, ByteBuffer buffer);
    
    /**
     * 回收
     */
    @Override
    void close();
    
    /**
     * 复制一个新的实例。
     * @return 
     */
    ChannelHandlerChain duplicate();
}
