/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.net;

import java.io.IOException;
import mano.Resettable;

/**
 * 表示一个一次性消息和处理它的方法。
 * @author jun <jun@diosay.com>
 */
public interface Message extends Resettable {
    /**
     * 处理该消息。
     * <p>注意：该消息是非线程安全的，所以请不要缓存任何方法参数。
     * @param <T>
     * @param channel
     * @param attachment 
     */
    <T> void process(IChannel channel,T attachment) throws IOException;
}
