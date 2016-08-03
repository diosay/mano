/*
 * Copyright (C) 2014 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.io.channel;


/**
 * 表示一个监听程序容器。
 * @author junhwong
 */
public interface ListenerManager {
    /**
     * 添加一个监听程序。
     * @param listener 
     */
    void add(Listener listener);
    
    /**
     * 移除一个监听程序。
     * @param listener 
     */
    void remove(Listener listener);
    
    /**
     * 获取当前监听程序的个数。
     * @return 
     */
    int listenerCount();
}
