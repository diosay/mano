/*
 * Copyright (C) 2014 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.io.channel;

import mano.io.Buffer;

/**
 * 表示一个消息通道。
 * @author junhwong
 */
public interface Channel {
    /**
     * 获取与之关联的监听器。
     * @return 
     */
    Listener getListener();
    /**
     * 从通道中读取消息。
     * @param message 
     */
    void read(Buffer message);
    /**
     * 将消息写入通道。
     * @param message 
     */
    void write(Buffer message);
    /**
     * 获取一个值，以指示当前通道是否打开。
     * @return 
     */
    boolean isOpen();
    /**
     * 关闭当前通道。
     */
    void close();
}
