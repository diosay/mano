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
 * 字节缓冲区消息。
 *
 * @author jun <jun@diosay.com>
 */
public class ByteBufferMessage<T extends Channel> implements Message<T> {

    public ChannelHanlder<T> handler;
    public ByteBuffer buffer;

    @Override
    public void process(T channel, ChannelHanlder<T> handler) throws IOException {
        channel.write(new ChannelBuffer(buffer), handler);
    }

    @Override
    public ChannelHanlder<T> getHandler() {
        return handler;
    }

    @Override
    public void reset() {
    }

}
