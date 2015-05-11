/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import mano.EventArgs;
import mano.EventHandler;
import mano.EventListener;
import mano.util.CachedObjectRecyler;
import mano.util.Pool;

/**
 *
 * @author sixmoon
 */
public abstract class ChannelListenerAbstract implements ChannelListener {

    private ChannelListenerContext context;

    protected EventListener.EventListenerHandle<ChannelListener, EventArgs> closedEventHandle = EventListener.create();
    protected boolean closed;
    protected boolean running;

    @Override
    public void setContext(ChannelListenerContext context) {
        if (context == null) {
            throw new IllegalArgumentException("context");
        }
        this.context = context;
    }

    public ChannelListenerContext getContext() {
        return context;
    }

    @Override
    public EventListener<EventHandler<ChannelListener, EventArgs>> closedEvent() {
        return closedEventHandle.getListener();
    }

    @Override
    public boolean isOpen() {
        return !closed;
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            onStop();
            closedEventHandle.fire(this, EventArgs.Empty);
            handlerChainPool.destory();
        }
    }

    @Override
    public abstract void bind(String address, int backlog) throws IOException ;

    @Override
    public final void run() {
        if (running || closed) {
            return;
        }
        running = true;
        try {
            onStart();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected abstract void onStart() throws Exception;

    protected abstract void onStop();

    
    
    
    private static final String err = "No more ChannelFilter.";
    private ChannelHandler[] handlers = new ChannelHandler[0];
    private final Pool<ChannelHandlerChain> handlerChainPool = new CachedObjectRecyler<ChannelHandlerChain>() {

        @Override
        protected ChannelHandlerChain createNew() {
            return new BasicChannelFilterChain();
        }
    };
    
    @Override
    public final void addHandler(ChannelHandler handler) {
        ChannelHandler[] tmp = new ChannelHandler[handlers.length + 1];
        System.arraycopy(handlers, 0, tmp, 0, handlers.length);
        tmp[handlers.length] = handler;
        handlers = tmp;
    }
    
    public ChannelHandlerChain getHandlerChain(){
        return handlerChainPool.get();
    }
    
    protected class BasicChannelFilterChain implements ChannelHandlerChain {

        private transient volatile int pos = 0;

        protected final boolean hasNext() {
            return pos + 1 <= handlers.length;
        }

        protected final ChannelHandler next() {
            return handlers[pos++];
        }

        @Override
        public void handleOpened(ChannelContext context) {
            if (hasNext()) {
                next().handleOpened(context, hasNext() ? this : null);
            } else {
                throw new IndexOutOfBoundsException(err);
            }
        }

        @Override
        public void handleClosed(ChannelContext context) {
            if (hasNext()) {
                next().handleClosed(context, hasNext() ? this : null);
            } else {
                throw new IndexOutOfBoundsException(err);
            }
        }

        @Override
        public void handleInbound(ChannelContext context, ByteBuffer buffer) {
            if (hasNext()) {
                next().handleInbound(context, hasNext() ? this : null, buffer);
            } else {
                throw new IndexOutOfBoundsException(err);
            }
        }

        @Override
        public void handleOutbound(ChannelContext context, ByteBuffer buffer) {
            if (hasNext()) {
                next().handleOutbound(context, hasNext() ? this : null, buffer);
            } else {
                throw new IndexOutOfBoundsException(err);
            }
        }

        @Override
        public final void close() {
            pos=0;
            handlerChainPool.put(this);
        }

        @Override
        public ChannelHandlerChain duplicate() {
            BasicChannelFilterChain chain = (BasicChannelFilterChain) handlerChainPool.get();
            chain.pos = pos;
            return chain;
        }
    }
    
    
}
