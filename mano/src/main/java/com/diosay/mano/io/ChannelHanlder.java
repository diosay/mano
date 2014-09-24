/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package com.diosay.mano.io;

import java.nio.ByteBuffer;

/**
 * 表示一个通道处理程序。
 *
 * @author jun <jun@diosay.com>
 */
public interface ChannelHanlder<T extends Channel> {

    void connected(T channel) throws Exception;
    
    void closed(T channel);
    
    void read(ChannelBuffer buffer,Integer reads,T channel) throws Exception;
    
    void written(ChannelBuffer buffer,Integer reads,T channel) throws Exception;
    
    void failed(Throwable exc, T channel);
}
