/*
 * Copyright (C) 2014 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.io.channel;

/**
 * 表示一个通道容器。
 * @author junhwong
 */
public interface ChannelManager {
    /**
     * 添加一个通道。
     * @param channel 
     */
    void add(Channel channel);
    
    /**
     * 移除一个通道。
     * @param channel 
     */
    void remove(Channel channel);
    
    /**
     * 获取当前通道的个数。
     * @return 
     */
    int channelCount();
}
