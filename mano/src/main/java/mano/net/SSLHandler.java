/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.net;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLException;
import mano.io.ByteBufferPool;
import mano.io.ChannelContext;
import mano.io.ChannelException;
import mano.io.ChannelHandler;
import mano.io.ChannelHandlerChain;
import mano.util.ArrayBlockingQueue;

/**
 *
 * @author sixmoon
 */
public class SSLHandler implements ChannelHandler {

    /**
     * 获取用于 SSL/TLS 通信过程中编/解码的默认(最小)缓冲区大小。
     */
    public static final int DEFAULT_BUFFER_SIZE = 17408;
    /**
     * 用于封包的空缓冲区。
     */
    protected static final ByteBuffer EMPTY_BUFFER = ByteBuffer.allocate(0);
    private static final String STATE_KEY = "--SSL_HANDSHAKE_STATUS--";

    SSLContext sslContext;
    ByteBufferPool bufferPool;

    public SSLHandler() throws SSLException {
        try {
            this.sslContext = SSLHelper.createSSLContext("SSL", "D:\\ssl\\www.diosay.com.jks", "DIOsay123321");
        } catch (Throwable ex) {
            throw new SSLException(ex);
        }
        this.bufferPool = new ByteBufferPool(DEFAULT_BUFFER_SIZE);
    }

    public SSLContext getContext() {
        return sslContext;
    }

    @Override
    public void handleConnected(ChannelContext context, ChannelHandlerChain chain) {
        try {
            HandshakeState state = new HandshakeState();
            state.context = context;
            state.engine = sslContext.createSSLEngine();
            state.engine.setUseClientMode(false);
            state.engine.setNeedClientAuth(false);
            state.engine.beginHandshake();
            state.status = state.engine.getHandshakeStatus();
            state.in = bufferPool.get();
            state.in.clear().flip();
            context.set(STATE_KEY, state);
            state.chain = chain.duplicate();
            context.channel().read();
            System.out.println("read0...");
            if (state.await()) {
                System.out.println("ok?????" + state.handshaked);
                chain.handleConnected(context);
            } else {
                throw new SSLException("handshake failed");
            }
        } catch (SSLException ex) {
            context.channel().close();
            ex.printStackTrace();
        } catch (ChannelException ex) {
            context.channel().close();
            ex.printStackTrace();
        }
    }

    @Override
    public void handleDisconnected(ChannelContext context, ChannelHandlerChain chain) {
        chain.handleDisconnect(context);
    }

    static void runDelegatedTask(HandshakeState state) {
        Runnable task;
        while ((task = state.engine.getDelegatedTask()) != null) {
            task.run();
        }
        state.status = state.engine.getHandshakeStatus();
    }

    @Override
    public void handleInbound(ChannelContext context, ChannelHandlerChain chain, ByteBuffer buffer) {
        HandshakeState state = (HandshakeState) context.get(STATE_KEY);
        try {
            state.unwrap(buffer);
        } catch (SSLException ex) {
            ex.printStackTrace();
        } catch (ChannelException ex) {
            ex.printStackTrace();
        }
    }

    void unwrap(final HandshakeState state, ChannelContext context, ChannelHandlerChain chain) throws ChannelException, SSLException {
        synchronized (state) {

            SSLEngineResult result;
            DONE:
            while (state.status != HandshakeStatus.NOT_HANDSHAKING) {
                System.out.println("Handshake:" + state.status);
                switch (state.status) {
                    case NEED_UNWRAP:
                        ByteBuffer buf = state.inboundQueue.poll();
                        if (buf == null) {
                            context.channel().read();
                            return;
                        }
                        ByteBuffer dst = bufferPool.get();
                        dst.clear();
                        result = state.engine.unwrap(buf, dst);
                        if (result.getStatus() == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
                            System.out.println("retry");
                            synchronized (state.inboundQueue) {
                                ByteBuffer tmp = bufferPool.get();
                                tmp.clear();
                                tmp.put(buf);
                                do {
                                    buf = state.inboundQueue.peek();
                                    if (buf != null && tmp.remaining() >= buf.remaining()) {
                                        tmp.put(buf);
                                        state.inboundQueue.poll();
                                        //todo free
                                    } else if (buf != null) {
                                        while (tmp.hasRemaining() && buf.hasRemaining()) {
                                            tmp.put(buf.get());
                                            if (!buf.hasRemaining()) {
                                                state.inboundQueue.poll();
                                                //todo free
                                            }
                                        }
                                    } else {
                                        break;
                                    }
                                } while (state.inboundQueue.isEmpty());
                                result = state.engine.unwrap(buf, dst);
                            }
                        }
                        if (buf.hasRemaining()) {
                            synchronized (state.inboundQueue) {
                                state.inboundQueue.add(0, buf);
                            }
                        } else {
                            //free
                        }
                        if (result.getStatus() == Status.BUFFER_UNDERFLOW) {
                            context.channel().read();
                            return;
                        } else if (result.getStatus() == Status.OK) {
                            state.status = result.getHandshakeStatus();
                        }
                        dst.flip();
                        if (dst.hasRemaining()) {
                            try (ChannelHandlerChain nc = chain.duplicate()) {
                                nc.handleInbound(context, dst);
                            }
                        }
                        System.out.println("RRRR" + result);
                        break;
                    case NEED_TASK:
                        runDelegatedTask(state);
                        break;
                    case NEED_WRAP:
                        ByteBuffer dst2 = bufferPool.get();
                        dst2.clear();
                        result = state.engine.wrap(EMPTY_BUFFER, dst2);
                        if (result.getStatus() != Status.OK) {
                            throw new SSLException("wrap result:" + result);
                        }
                        dst2.flip();
                        System.out.println("wrap..." + dst2);
                        if (dst2.hasRemaining()) {
                            context.putOutbound(dst2);
                        }
                        System.out.println("SSS" + result);
                        state.status = result.getHandshakeStatus();
                        break;
                    case FINISHED:
                        state.isHandshaked = true;
                        break DONE;
                }
            }

            if (state.isHandshaked && !state.inboundQueue.isEmpty()) {
                ByteBuffer buf = state.inboundQueue.poll();
                if (buf == null) {
                    return;
                }
                ByteBuffer dst = bufferPool.get();
                dst.clear();
                result = state.engine.wrap(buf, dst);
                if (result.getStatus() != Status.OK) {
                    //free
                    throw new SSLException("wrap");
                }
                dst.flip();
                if (dst.hasRemaining()) {
                    try (ChannelHandlerChain nc = chain.duplicate()) {
                        nc.handleInbound(context, dst);
                    }
                }
            }

            if (state.isHandshaked && !state.outboundQueue.isEmpty()) {
                try (ChannelHandlerChain nc = chain.duplicate()) {
                    wrap(state, context, nc);
                }
            }
        }
    }

    void wrap(final HandshakeState state, ChannelContext context, ChannelHandlerChain chain) throws SSLException {
        synchronized (state) {
            if (state.isHandshaked) {
                return;
            }
            ByteBuffer buf = state.outboundQueue.poll();
            if (buf != null) {
                ByteBuffer dst = bufferPool.get();
                dst.clear();
                SSLEngineResult result = state.engine.wrap(buf, dst);
                if (result.getStatus() != Status.OK) {
                    //free
                    throw new SSLException("wrap");
                }
                dst.flip();
                if (state.outboundQueue.isEmpty()) {
                    chain.handleOutbound(context, buf);
                } else {
                    ChannelHandlerChain nc = chain.duplicate();
                    chain.handleOutbound(context, buf);
                    wrap(state, context, nc);
                }

            }
        }

    }

    @Override
    public void handleOutbound(ChannelContext context, ChannelHandlerChain chain, ByteBuffer buffer) {
        HandshakeState state = (HandshakeState) context.get(STATE_KEY);
        try {
            state.wrap(buffer);
        } catch (SSLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void handleError(ChannelContext context, ChannelHandlerChain chain, Throwable cause) {
        chain.handleError(context, cause);
    }

    @Override
    public void setProperty(String property, Object value) {
        
    }

    @Override
    public void init() {
    }

    @Override
    public void destroy() {
    }

    class HandshakeState {

        ChannelContext context;
        SSLEngine engine;
        HandshakeStatus status;
        boolean isHandshaked;
        final java.util.concurrent.atomic.AtomicBoolean handshaked = new java.util.concurrent.atomic.AtomicBoolean(false);
        private final ArrayBlockingQueue<ByteBuffer> inboundQueue = new ArrayBlockingQueue<>();
        private final ArrayBlockingQueue<ByteBuffer> outboundQueue = new ArrayBlockingQueue<>();
        ByteBuffer in;
        ByteBuffer out;
        ByteBuffer keep;
        ChannelHandlerChain chain;
        private final ReentrantLock inLock = new ReentrantLock();
        private final ReentrantLock outLock = new ReentrantLock();

        void unwrap2(ByteBuffer buffer) throws SSLException, ChannelException {
            SSLEngineResult result;
            while (buffer.hasRemaining()) {
                inLock.lock();
                try {
                    System.out.println("in x" + in);
                    in.compact();
                    if (in.remaining() >= buffer.remaining()) {
                        in.put(buffer);
                    } else if (buffer.hasArray()) {
                        int count = in.remaining();
                        in.put(buffer.array(), buffer.arrayOffset() + buffer.position(), in.remaining());
                        buffer.position(buffer.position() + count);
                    } else {
                        while (in.hasRemaining() && buffer.hasRemaining()) {
                            in.put(buffer.get());
                        }
                    }
                    in.flip();
                    System.out.println("in y" + in);
                    while (in.hasRemaining() && (handshaked.get() || status == HandshakeStatus.NEED_UNWRAP)) {
                        ByteBuffer dst = bufferPool.get();
                        dst.clear();

                        do {
                            result = engine.unwrap(in, dst);
                        } while (result.getStatus() == SSLEngineResult.Status.OK
                                && result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_UNWRAP
                                && result.bytesProduced() == 0);
                        System.out.println("in z" + in);
                        status = result.getHandshakeStatus();
                        if (status == HandshakeStatus.NEED_UNWRAP && result.getStatus() == Status.BUFFER_UNDERFLOW) {
                            if (buffer.hasRemaining()) {
                                throw new SSLException("err buff:" + result);
                            }
                            context.channel().read();
                            System.out.println("read1...");
                            return;
                        } else if (result.getStatus() != Status.OK) {
                            throw new SSLException("unwrap result:" + result);
                        }

                        dst.flip();
                        if (dst.hasRemaining()) {
                            try (ChannelHandlerChain tmp = chain.duplicate()) {
                                tmp.handleInbound(context, dst);
                            }
                        }
                        loop();
                    }
                    loop();
                    if (status == HandshakeStatus.NEED_UNWRAP && !buffer.hasRemaining()) {
                        context.channel().read();
                        System.out.println("read2...");
                    }
                } finally {
                    inLock.unlock();
                }
            }
            //

        }

        void unwrap(ByteBuffer buffer) throws SSLException, ChannelException {
            ByteBuffer dst = bufferPool.get();
            inLock.lock();
            try {
                if (keep != null) {
                    keep.compact();
                    if (keep.remaining() >= buffer.remaining()) {
                        keep.put(buffer);
                        context.free(buffer);
                        buffer = keep;
                    } else {
                        ByteBuffer swap = bufferPool.get();
                        swap.clear();
                        keep.flip();
                        swap.put(keep);
                        if (keep.capacity() == bufferPool.getBufferSize()) {
                            bufferPool.put(keep);
                        } else {
                            context.free(keep);
                        }
                        swap.put(buffer);
                        context.free(buffer);
                        buffer = swap;
                    }
                    buffer.flip();
                    keep = null;
                }
                SSLEngineResult result;

                while (buffer.hasRemaining() && (handshaked.get() || status == HandshakeStatus.NEED_UNWRAP)) {
                    dst.clear();
                    result = engine.unwrap(buffer, dst);
                    status = result.getHandshakeStatus();
                    if (status == HandshakeStatus.NEED_UNWRAP && result.getStatus() == Status.BUFFER_UNDERFLOW) {
                        keep = buffer;
                        context.channel().read();
                        return;
                    } else if (result.getStatus() != Status.OK) {
                        throw new SSLException("unwrap result:" + result);
                    }
                    dst.flip();
                    emitIn(dst);
                    loop();
                }

                if (buffer.hasRemaining()) {
                    keep = buffer;
                } else {
                    if (buffer.capacity() == bufferPool.getBufferSize()) {
                        bufferPool.put(buffer);
                    } else {
                        context.free(buffer);
                    }
                }
                if (status == HandshakeStatus.NEED_UNWRAP) {
                    context.channel().read();
                }
            } finally {
                bufferPool.put(dst);
                inLock.unlock();
            }
        }

        void wrap(ByteBuffer buffer) throws SSLException {
            SSLEngineResult result;
            ByteBuffer dst = bufferPool.get();
            while (buffer.hasRemaining()) {
                outLock.lock();
                try {
                    if (await()) {
                        dst.clear();
                        result = engine.wrap(buffer, dst);
                        if (result.getStatus() != Status.OK) {
                            throw new SSLException("wrap result:" + result);
                        }
                        dst.flip();
                        emitOut(dst);
                        loop();
                    }
                } finally {
                    bufferPool.put(dst);
                    outLock.unlock();
                }
            }
        }

        private void copyTo(ByteBuffer src, ByteBuffer dst) {
            if (dst.remaining() >= src.remaining()) {
                dst.put(src);
            } else {
                if (src.hasArray()) {
                    int count = dst.remaining();
                    dst.put(src.array(), src.arrayOffset() + src.arrayOffset(), count);
                    src.position(src.position() + count);
                } else {
                    while (dst.hasRemaining() && src.hasRemaining()) {
                        dst.put(src.get());
                    }
                }
            }
        }

        private void emitIn(ByteBuffer buffer) {
            ByteBuffer dst;
            while (buffer.hasRemaining()) {
                try (ChannelHandlerChain tmp = chain.duplicate()) {
                    dst = context.allocate();
                    dst.clear();
                    copyTo(buffer, dst);
                    dst.flip();
                    tmp.handleInbound(context, dst);
                }
            }
        }

        private void emitOut(ByteBuffer buffer) {
            ByteBuffer dst;
            while (buffer.hasRemaining()) {
                try (ChannelHandlerChain tmp = chain.duplicate()) {
                    dst = context.allocate();
                    dst.clear();
                    copyTo(buffer, dst);
                    dst.flip();
                    tmp.handleOutbound(context, dst);
                }
            }
        }

        private void loop() throws SSLException {
            SSLEngineResult result;
            DONE:
            while (status != HandshakeStatus.NOT_HANDSHAKING) {
                if (status == HandshakeStatus.FINISHED) {
                    if (!handshaked.get()) {
                        synchronized (handshaked) {
                            handshaked.set(true);
                            handshaked.notify();
                        }
                        System.out.println("done");
                    }
                    System.out.println("222");
                    return;
                } else {
                    handshaked.set(false);
                }
                System.out.println("Handshake:" + status);
                switch (status) {
                    case NEED_UNWRAP:
                        return;
                    case NEED_WRAP: {
                        ByteBuffer dst = bufferPool.get();
                        dst.clear();
                        result = engine.wrap(EMPTY_BUFFER, dst);
                        if (result.getStatus() != Status.OK) {
                            throw new SSLException("wrap result:" + result);
                        }
                        dst.flip();
                        System.out.println("wrap..." + dst);
                        if (dst.hasRemaining()) {
                            context.putOutbound(dst);
                        }
                        status = result.getHandshakeStatus();
                    }
                    break;
                    case NEED_TASK:
                        Runnable task;
                        while ((task = engine.getDelegatedTask()) != null) {
                            task.run();
                        }
                        status = engine.getHandshakeStatus();
                        break;
                }
            }
        }

        void handshake(ChannelHandlerChain chain) throws ChannelException, InterruptedException, SSLException {
            SSLEngineResult result;
            DONE:
            while (status != HandshakeStatus.NOT_HANDSHAKING) {
                System.out.println("Handshake:" + status);
                switch (status) {
                    case NEED_UNWRAP: {
                        ByteBuffer buf;
                        do {
                            buf = inboundQueue.poll();
                            if (buf == null) {
                                context.channel().read();
                                synchronized (inboundQueue) {
                                    inboundQueue.wait();
                                    buf = inboundQueue.poll();
                                }
                            }
                            if (buf != null) {
                                break;
                            }
                        } while (buf == null);
                        System.out.println("in buf:" + buf);
                        ByteBuffer dst = bufferPool.get();
                        dst.clear();
                        result = engine.unwrap(buf, dst);
                        if (result.getStatus() != Status.OK) {
                            throw new SSLException("unwrap result:" + result);
                        }
                        status = result.getHandshakeStatus();
                        dst.flip();
                        if (dst.hasRemaining()) {
                            try (ChannelHandlerChain tmp = chain.duplicate()) {
                                tmp.handleInbound(context, dst);
                            }
                        }
                    }
                    break;
                    case NEED_WRAP: {
                        ByteBuffer dst = bufferPool.get();
                        dst.clear();
                        result = engine.wrap(EMPTY_BUFFER, dst);
                        if (result.getStatus() != Status.OK) {
                            throw new SSLException("wrap result:" + result);
                        }
                        dst.flip();
                        System.out.println("wrap..." + dst);
                        if (dst.hasRemaining()) {
                            context.putOutbound(dst);
                        }
                        status = result.getHandshakeStatus();
                    }
                    break;
                    case NEED_TASK:
                        Runnable task;
                        while ((task = engine.getDelegatedTask()) != null) {
                            task.run();
                        }
                        status = engine.getHandshakeStatus();
                        break;
                    case FINISHED:
                        synchronized (handshaked) {
                            handshaked.set(true);
                            handshaked.notify();
                        }
                        System.out.println("done");
                        break DONE;
                }
            }

            while (handshaked.get() && !inboundQueue.isEmpty()) {
                ByteBuffer buf = inboundQueue.poll();
                if (buf == null) {
                    break;
                }
                ByteBuffer dst = bufferPool.get();
                dst.clear();
                result = engine.unwrap(buf, dst);
                if (result.getStatus() != Status.OK) {
                    throw new SSLException("wrap result:" + result);
                }
                status = engine.getHandshakeStatus();
                dst.flip();
                if (dst.hasRemaining()) {
                    try (ChannelHandlerChain tmp = chain.duplicate()) {
                        tmp.handleInbound(context, dst);
                    }
                }
            }

            while (handshaked.get() && !outboundQueue.isEmpty()) {
                ByteBuffer buf = outboundQueue.poll();
                if (buf == null) {
                    break;
                }
                ByteBuffer dst = bufferPool.get();
                dst.clear();
                result = engine.wrap(buf, dst);
                if (result.getStatus() != Status.OK) {
                    throw new SSLException("wrap result:" + result);
                }
                status = engine.getHandshakeStatus();
                dst.flip();
                if (dst.hasRemaining()) {
                    try (ChannelHandlerChain tmp = chain.duplicate()) {
                        tmp.handleOutbound(context, dst);
                    }
                }
            }

        }

        boolean await() {
            synchronized (handshaked) {
                if (!handshaked.get()) {
                    try {
                        handshaked.wait(1000 * 5);
                    } catch (InterruptedException ex) {
                        return false;
                    }
                }
                return handshaked.get();
            }
        }
    }

}
