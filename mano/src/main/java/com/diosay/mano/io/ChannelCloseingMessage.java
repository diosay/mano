/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package com.diosay.mano.io;

import java.io.IOException;
import mano.util.LockState;

/**
 * 关闭通道
 *
 * @author jun <jun@diosay.com>
 */
public class ChannelCloseingMessage<T extends Channel> implements Message<T> {

    public ChannelHanlder<T> handler;

    @Override
    public void process(T channel, LockState state) throws IOException {
        System.out.println("close channel");
        channel.close();
        state.notifyDone();
        
    }

    @Override
    public ChannelHanlder<T> getHandler() {
        return handler;
    }

    @Override
    public void reset() {
    }

}
