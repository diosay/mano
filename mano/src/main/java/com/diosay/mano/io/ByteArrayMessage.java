/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package com.diosay.mano.io;

import java.io.IOException;

/**
 * 字节组消息
 *
 * @author jun <jun@diosay.com>
 */
public class ByteArrayMessage<T extends Channel> implements Message<T> {

    public byte[] array;
    public int offset;
    public int length;
    public ChannelHanlder<T> handler;

    @Override
    public void reset() {
    }

    @Override
    public void process(T channel, ChannelHanlder<T> handler) throws IOException {
        channel.write(new ChannelBuffer(array, offset, length), handler);
    }

    @Override
    public ChannelHanlder<T> getHandler() {
        return handler;
    }
}