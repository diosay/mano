/*
 * Copyright (C) 2014 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.io.channel;

import mano.io.Buffer;

/**
 * 表示一个通道事件处理程序。
 * @author junhwong
 */
public interface Hanlder<T extends Channel,B extends Buffer> {
    /**
     * 当通道成功添加到管理程序({@see mano.io.channel.ChannelManager})中时触发。
     * @param channel 
     */
    void onAdded(T channel);
    /**
     * 当通道成功从管理程序({@see mano.io.channel.ChannelManager})中移除时触发。
     * @param channel 
     */
    void onRemoved(T channel);
    /**
     * 当通道创建并连接时触发。
     * @param channel 
     */
    void onConnected(T channel);
    /**
     * 当通道关闭时触发。
     * @param channel 
     */
    void onClosed(T channel);
    
    void onRead(T channel,Buffer buffer);
    void onWritten(T channel);
    void onFailed(T channel,Throwable exception, Object attachment);
    void onPromised(T channel);
}
