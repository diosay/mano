/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */

package com.diosay.mano.web;

import com.diosay.mano.io.Channel;
import com.diosay.mano.io.Listener;
import java.nio.channels.AsynchronousSocketChannel;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class HttpListener extends Listener{

    @Override
    protected Channel create(AsynchronousSocketChannel remote) {
        return new HttpChannel(remote,this);
    }
    
}
