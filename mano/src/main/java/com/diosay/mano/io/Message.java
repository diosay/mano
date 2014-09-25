/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */

package com.diosay.mano.io;

import java.io.IOException;
import mano.Resettable;
import mano.util.LockState;

/**
 * 表示一个消息载体和处理消息的方法。
 * @author jun <jun@diosay.com>
 */
public interface Message<T extends Channel> extends Resettable {
    /**
     * 处理该消息。
     * <p>注意：该消息是非线程安全的，所以请不要缓存任何方法参数。
     * @param <T>
     * @param state
     * @param attachment 
     */
    void process(T channel,LockState state) throws IOException;
    
    ChannelHanlder<T> getHandler();
}
