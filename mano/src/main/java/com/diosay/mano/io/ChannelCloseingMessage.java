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
 * 关闭通道
 *
 * @author jun <jun@diosay.com>
 */
public class ChannelCloseingMessage<T extends Channel> implements Message<T> {

    public ChannelHanlder<T> handler;

    @Override
    public void process(T channel, ChannelHanlder<T> handler) throws IOException {
        channel.close();
        handler.closed(channel);
    }

    @Override
    public ChannelHanlder<T> getHandler() {
        return handler;
    }

    @Override
    public void reset() {
    }

}
