/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.io;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 与 {@link Channel} 关联的IO操作上下文。
 * @author junman
 */
public interface ChannelContext {
    
    Channel channel();
    
    void filterInbound(ByteBuffer buffer);
    
    void filterOutbound(ByteBuffer buffer);
    
    int send(ByteBuffer buffer) throws IOException;
    
    int recv(ByteBuffer buffer) throws IOException;
    
    Object get(String key);
    void set(String key,Object value);
    //Object remove(String key);
    
    ByteBuffer allocBuffer();
    void freeBuffer(ByteBuffer buffer);
    
    void release();
    
    void handleError(Throwable cause);
    
    ChannelFuture createWriteFuture(ByteBuffer buffer);
    
}
