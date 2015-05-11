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
 * @author sixmoon
 */
public interface ChannelContextOld {

    Channel channel();

    void putInbound(ByteBuffer buffer);

    void putOutbound(ByteBuffer buffer);

    void copyOnPutInbound(ByteBuffer buffer);

    void copyOnPutOutbound(ByteBuffer buffer);

    Object get(Object key);

    void set(Object key, Object value);
    
    BufferManager getBufferManager();
    
    ByteBuffer allocate();
    void free(ByteBuffer buffer);
}
