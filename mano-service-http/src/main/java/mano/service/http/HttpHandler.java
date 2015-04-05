/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.service.http;

import java.nio.ByteBuffer;
import mano.io.ChannelContext;
import mano.io.ChannelException;
import mano.io.ChannelHandler;
import mano.io.ChannelHandlerChain;

/**
 *
 * @author sixmoon
 */
public class HttpHandler implements ChannelHandler {

    private static final String CONTEXT_KEY = "--HTTP_CONTEXT--";

    @Override
    public void setProperty(String property, Object value) {
    }

    @Override
    public void init() throws Exception {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void handleConnected(ChannelContext context, ChannelHandlerChain chain) {
        HttpContextImpl ctx = new HttpContextImpl();
        ctx.setChannel(context.channel());
        context.set(CONTEXT_KEY, ctx);
        try {
            context.channel().read();
        } catch (ChannelException ex) {
            try (ChannelHandlerChain chc = chain.duplicate()) {
                chc.handleError(context, ex);
            }
        }
    }

    @Override
    public void handleDisconnected(ChannelContext context, ChannelHandlerChain chain) {
        context.set(CONTEXT_KEY, null);

    }

    @Override
    public void handleInbound(ChannelContext context, ChannelHandlerChain chain, ByteBuffer buffer) {
        if (!buffer.hasRemaining()) {
            context.free(buffer);
            return;
        }
        HttpContextImpl ctx = (HttpContextImpl) context.get(CONTEXT_KEY);
        if (ctx == null) {
            //throw err
            context.free(buffer);
            context.channel().close();
            return;
        }
        try {
            ctx.onRead(buffer, context.getBufferManager());
        } catch (ChannelException ex) {
            try (ChannelHandlerChain chc = chain.duplicate()) {
                chc.handleError(context, ex);
            }
        }
    }

    @Override
    public void handleOutbound(ChannelContext context, ChannelHandlerChain chain, ByteBuffer buffer) {
        if (buffer.hasRemaining()) {
            context.putOutbound(buffer);
        } else {
            context.free(buffer);
        }
    }

    @Override
    public void handleError(ChannelContext context, ChannelHandlerChain chain, Throwable cause) {
        cause.printStackTrace();
    }

}
