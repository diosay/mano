/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.io;

import java.io.IOException;
import mano.EventArgs;
import mano.EventHandler;
import mano.EventListener;

/**
 *
 * @author sixmoon
 */
public interface ChannelListener extends Runnable {

    EventListener<EventHandler<ChannelListener, EventArgs>> closedEvent();

    boolean isOpen();

    void close();

    void setContext(ChannelListenerContext context) throws IllegalArgumentException;

    void bind(String address, int backlog) throws IOException;

    void addHandler(ChannelHandler filter) throws IllegalArgumentException;
}
