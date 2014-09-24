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
 * 文件范围消息。
 *
 * @author jun <jun@diosay.com>
 */
public class FileReginMessage<T extends Channel> implements Message<T> {

    public ChannelHanlder<T> handler;
    public String filename;
    public long position;
    public long length;

    @Override
    public void process(T channel, ChannelHanlder<T> handler) throws IOException {
        channel.write(filename, position, length, handler);
    }

    @Override
    public ChannelHanlder<T> getHandler() {
        return handler;
    }

    @Override
    public void reset() {
    }

}
