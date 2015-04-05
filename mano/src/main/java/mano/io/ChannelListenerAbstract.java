/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.io;

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

    protected class BasicChannelHandlerChain implements ChannelHandlerChain {

        private transient volatile int pos = 0;

        protected final boolean hasNext() {
            return pos + 1 <= handlers.length;
        }

        protected final ChannelHandler next() {
            return handlers[pos++];
        }

        @Override
        public void handleConnected(ChannelContext context) {
            if (hasNext()) {
                next().handleConnected(context, hasNext() ? this : null);
            } else {
                throw new IndexOutOfBoundsException(err);
            }
        }

        @Override
        public void handleDisconnect(ChannelContext context) {
            if (hasNext()) {
                next().handleDisconnected(context, hasNext() ? this : null);
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
        public void handleError(ChannelContext context, Throwable cause) {
            if (hasNext()) {
                next().handleError(context, hasNext() ? this : null, cause);
            } else {
                throw new IndexOutOfBoundsException(err);
            }
        }

        @Override
        public final void close() {
            putHandlerChain(this);
        }

        @Override
        public ChannelHandlerChain duplicate() {
            BasicChannelHandlerChain chain = (BasicChannelHandlerChain) getHandlerChain();
            chain.pos = pos;
            return chain;
        }
    }

    private ChannelListenerContext context;
    private static final String err = "No more ChannelHandlerChain.";
    private ChannelHandler[] handlers = new ChannelHandler[0];
    private final Pool<ChannelHandlerChain> handlerChainPool = new CachedObjectRecyler<ChannelHandlerChain>() {

        @Override
        protected ChannelHandlerChain createNew() {
            return createNewHandlerChain();
        }
    };
    protected EventListener.EventListenerHandle<ChannelListener, EventArgs> closedEventHandle;
    protected boolean closed;
    protected boolean running;
    public ChannelListenerAbstract() {
        closedEventHandle = EventListener.create();
    }
    
    @Override
    public void setContext(ChannelListenerContext context){
        if(context==null){
            throw new IllegalArgumentException("context");
        }
        this.context=context;
    }
    
    public ChannelListenerContext getContext(){
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
            handlerChainPool.destory();
            closedEventHandle.fire(this, EventArgs.Empty);
        }
    }

    @Override
    public abstract void bind(String address, int backlog);

    @Override
    public final void addHandler(ChannelHandler handler) {
        ChannelHandler[] tmp = new ChannelHandler[handlers.length + 1];
        System.arraycopy(handlers, 0, tmp, 0, handlers.length);
        tmp[handlers.length] = handler;
        handlers = tmp;
    }

    @Override
    public final void run() {
        if(running || closed){
            return;
        }
        running=true;
        try {
            onStart();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    protected abstract void onStart() throws Exception;
    protected abstract void onStop();

    protected BasicChannelHandlerChain createNewHandlerChain() {
        return new BasicChannelHandlerChain();
    }

    public final ChannelHandlerChain getHandlerChain() {
        ChannelHandlerChain chain = handlerChainPool.get();
        ((BasicChannelHandlerChain) chain).pos = 0;
        return chain;
    }

    public final boolean putHandlerChain(ChannelHandlerChain chain) {
        if (chain != null && chain instanceof BasicChannelHandlerChain) {
            return handlerChainPool.put(chain);
        }
        return false;
    }
    
    

}
