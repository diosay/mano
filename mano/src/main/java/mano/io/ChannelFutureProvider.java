/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.io;

/**
 *
 * @author jun
 */
public interface ChannelFutureProvider {
    void release(ChannelFuture future);
}
