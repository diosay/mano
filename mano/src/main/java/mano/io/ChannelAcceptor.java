/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.io;

/**
 *
 * @author sixmoon
 */
public interface ChannelAcceptor extends Runnable, java.nio.channels.Channel {

    boolean isOpen();

    void close();
}
