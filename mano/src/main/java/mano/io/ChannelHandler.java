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
public interface ChannelHandler {

    void setProperty(String property,Object value);
    
    void init() throws Exception;
    
    void destroy();
    
    void handleOpened(ChannelContext context, ChannelHandlerChain chain);

    void handleClosed(ChannelContext context, ChannelHandlerChain chain);

    void handleInbound(ChannelContext context, ChannelHandlerChain chain, ByteBuffer buffer);

    void handleOutbound(ChannelContext context, ChannelHandlerChain chain, ByteBuffer buffer);
}
