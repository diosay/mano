/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package com.diosay.mano.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.nio.channels.InterruptedByTimeoutException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.WritePendingException;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import mano.util.LockState;
import mano.util.OptimisticLocker;
import mano.util.ThreadPool;

/**
 * 异步套接字通道。
 *
 * @author jun <jun@diosay.com>
 */
public class AioSocketChannel implements Channel {

    AsynchronousSocketChannel inner;
    Listener listener;
    protected final Queue<Message> paddings = new LinkedBlockingQueue<>();
    protected Semaphore writeLocker2 = new Semaphore(1);
    protected Semaphore readLocker = new Semaphore(1);
    final AtomicBoolean writePadding = new AtomicBoolean(false);
    final OptimisticLocker writeLocker = new OptimisticLocker();
    private TransferProxy proxy;

    public AioSocketChannel(AsynchronousSocketChannel channel, Listener listener) {
        inner = channel;
        proxy = new TransferProxy();
        proxy.channel = channel;
        this.listener = listener;
    }

    @Override
    public void enqueue(Message message) {
        paddings.offer(message);
        flush();
    }

    protected void flush() {
        ThreadPool.execute(new Runnable() {

            @Override
            public void run() {
                synchronized (paddings) {
                    if (paddings.isEmpty()) {
                        return;
                    }
                    LockState state = writeLocker.acquire(1000 * 60 * 5);
                    if (state == null) {
                        flush();//try
                        return;
                    }
                    Message msg = paddings.poll();
                    if (msg != null) {
                        try {
                            msg.process(AioSocketChannel.this, state);
                        } catch (Exception ex) {
                            ChannelHanlder handler = getListener().getGroup().getHandler();
                            try {
                                handler.failed(ex, AioSocketChannel.this);
                            } catch (Exception ex2) {
                                failed(ex2);
                            }
                            handler = null;
                        } finally {
                            readLocker.release();
                        }
                        flushed(msg);
                        msg = null;
                    } else {
                        writeLocker.release(state);
                    }
                }
                flush();
            }
        }
        );
    }

    protected void flushed(Message msg) {
    }

    protected void failed(Throwable ex) {
        ex.printStackTrace();
    }

    @Override
    public void close() {

        try {
            //Thread.sleep(1000 * 5);
            inner.close();
        } catch (Throwable ex) {
            //ignored
        } finally {
            inner = null;
            listener.getGroup().remove(this);
        }
    }

    @Override
    public void clear() {
        synchronized (paddings) {
            try {
                paddings.forEach((msg) -> {
                    flushed(msg);
                });
            } catch (Throwable ex) {
                //ignored
            }
            paddings.clear();
        }
    }

    @Override
    public boolean isOpen() {
        if (inner == null) {
            return false;
        }
        return inner.isOpen();
    }

    @Override
    public Listener getListener() {
        return listener;
    }

    @Override
    public void read(ChannelBuffer buffer) throws IOException {

        if (!isOpen()) {
            throw new ClosedChannelException();
        }
        try {
            if (!readLocker.tryAcquire(5, TimeUnit.MINUTES)) {
                throw new InterruptedByTimeoutException();
            }
        } catch (InterruptedException ex) {
            throw new InterruptedByTimeoutException();
        }

        try {
            inner.read(buffer.buffer, 5, TimeUnit.SECONDS, buffer, new CompletionHandler<Integer, Object>() {
                @Override
                public void completed(Integer result, Object obj) {

                    if (result < 0) {
                        failed(new ClosedChannelException(), obj);
                    } else {
                        readLocker.release();
                        ChannelHanlder handler = getListener().getGroup().getHandler();
                        try {
                            buffer.buffer.flip();

                            handler.read(buffer, result, AioSocketChannel.this);
                        } catch (Exception ex) {
                            handler.failed(ex, AioSocketChannel.this);
                        }
                    }
                }

                @Override
                public void failed(Throwable exc, Object obj) {
                    ChannelHanlder handler = getListener().getGroup().getHandler();
                    try {
                        handler.failed(exc, AioSocketChannel.this);
                    } finally {
                        readLocker.release();
                    }
                }
            });
        } finally {
            readLocker.release();
        }
    }

    @Override
    public void write(ChannelBuffer buffer, LockState state) throws IOException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
        this.write0(buffer, state);
    }

    @Override
    public void write(String filename, long position, long length, LockState state) throws IOException {

        if (!isOpen()) {
            throw new ClosedChannelException();
        }

        long sent;
        try (FileInputStream in = new FileInputStream(filename)) {
            sent = in.getChannel().transferTo(position, length, proxy);
        }
        state.notifyDone();
        ChannelHanlder handler = getListener().getGroup().getHandler();
        try {
            handler.written(null, (int) sent, this);
        } catch (Exception ex) {
            handler.failed(ex, AioSocketChannel.this);
        }
    }

    private void write0(ChannelBuffer buffer, LockState state) throws IOException {

        inner.write(buffer.buffer, 5, TimeUnit.SECONDS, buffer, new CompletionHandler<Integer, Object>() {
            @Override
            public void completed(Integer result, Object obj) {

                if (result < 0) {
                    failed(new ClosedChannelException(), obj);
                } else if (buffer.buffer.hasRemaining()) {
                    try {
                        write0(buffer, state);
                    } catch (IOException ex) {
                        failed(ex, obj);
                    }
                } else {
                    state.notifyDone();
                    ChannelHanlder handler = getListener().getGroup().getHandler();
                    try {
                        handler.written(buffer, result, AioSocketChannel.this);
                    } catch (Exception ex) {
                        handler.failed(ex, AioSocketChannel.this);
                    }
                }
            }

            @Override
            public void failed(Throwable exc, Object obj) {
                state.notifyDone();
                ChannelHanlder handler = getListener().getGroup().getHandler();
                handler.failed(exc, AioSocketChannel.this);
            }
        });
    }

    private static class TransferProxy implements ReadableByteChannel, WritableByteChannel {

        AsynchronousSocketChannel channel;

        @Override
        public int read(ByteBuffer dst) throws IOException {
            try {
                Future<Integer> result = channel.read(dst);
                return result.get(1000 * 5, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                throw new IOException(ex.getMessage(), ex);
            }
        }

        @Override
        public boolean isOpen() {
            return channel.isOpen();
        }

        @Override
        public void close() throws IOException {
            channel = null; //调用者自己关闭
        }

        @Override
        public int write(ByteBuffer src) throws IOException {
            return write(src, true, 0);
        }

        private int write(ByteBuffer src, boolean cantry, int tried) throws IOException {
            try {
                Future<Integer> result = channel.write(src);
                int count = result.get(1000 * 5, TimeUnit.MILLISECONDS);
                //Logger.getDefault().info("sent:%s", count);
                return count;
            } catch (WritePendingException ex) {
                if (cantry && tried < 50) {
                    return this.write(src, cantry, tried++);
                }
                throw new IOException(ex.getMessage(), ex);
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                throw new IOException(ex.getMessage(), ex);
            }
        }

    }

}
