/*
 * Copyright (C) 2014 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.io.channel;

import mano.io.Buffer;
import mano.io.channel.Channel;

/**
 *
 * @author junhwong
 */
public interface Listener {
    void handleConnected();
    void handleRead(Buffer buffer,Channel channel);
    void handleWritten();
    void handleFailed();
}
