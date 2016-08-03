/*
 * Copyright (C) 2014 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.net;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketOption;
import mano.io.channel.Channel;

/**
 *
 * @author junhwong
 */
public interface SocketChannel extends Channel {
    SocketAddress getLocalAddress() throws IOException;
    SocketAddress getRemoteAddress() throws IOException;
    <T> AioSocketChannel setOption(SocketOption<T> name, T value) throws IOException;
    <T> T getOption(SocketOption<T> name) throws IOException;
}
