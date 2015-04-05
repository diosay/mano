/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.net;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.util.Set;
import mano.io.Channel;

/**
 * 表示一个套接字通道。
 * @author sixmoon
 */
public interface SocketChannel extends Channel {
    
    /**
     * 设置套接字的参数。
     * @param <T>
     * @param name
     * @param value
     * @return
     * @throws IOException 
     */
    <T> SocketChannel setOption(SocketOption<T> name, T value) throws IOException;
    
    /**
     * 获取套接字支持的参数。
     */
    Set<SocketOption<?>> supportedOptions();
    
    /**
     * 获取本地地址。
     * @return
     * @throws IOException 
     */
    SocketAddress getLocalAddress() throws IOException;
    
    /**
     * 获取远程主机地址。
     * @return
     * @throws IOException 
     */
    SocketAddress getRemoteAddress() throws IOException;
    
}
