/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
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
    protected Semaphore writeLocker = new Semaphore(1);
    protected Semaphore readLocker = new Semaphore(1);
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
                    Message msg = paddings.poll();
                    if (msg != null) {
                        try {
                            msg.process(AioSocketChannel.this, msg.getHandler());
                        } catch (IOException ex) {
                            //onFailed(ex, msg);
                        }
                        onFlushed(msg);
                        msg = null;
                    }
                }
            }
        });
    }

    protected void onFlushed(Message msg) {
    }

    @Override
    public <T extends Channel> void write(String filename, long position, long length, ChannelHanlder<T> handler) throws IOException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
        try {
            if (!writeLocker.tryAcquire(5, TimeUnit.MINUTES)) {
                throw new InterruptedByTimeoutException();
            }
        } catch (InterruptedException ex) {
            throw new InterruptedByTimeoutException();
        }
        long sent = -1;
        try {
            try (FileInputStream in = new FileInputStream(filename)) {
                sent = in.getChannel().transferTo(position, length, proxy);
            }
        } finally {
            writeLocker.release();
        }
        
        try {
            handler.written(null, (int) sent, (T) this);
        } catch (Exception ex) {
            handler.failed(ex, (T) AioSocketChannel.this);
        }

    }

    @Override
    public void close() {
        try {
            inner.close();
        } catch (IOException ex) {
            //ignored
        } finally {
            inner = null;
        }
    }

    @Override
    public void clear() {
        synchronized (paddings) {
            try {
                paddings.forEach((msg) -> {
                    onFlushed(msg);
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
    public <T extends Channel> void read(ChannelBuffer buffer, ChannelHanlder<T> handler) throws IOException {

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
        inner.read(buffer.buffer, 5, TimeUnit.SECONDS, handler, new CompletionHandler<Integer, ChannelHanlder<T>>() {
            @Override
            public void completed(Integer result, ChannelHanlder<T> handler) {

                if (result < 0) {
                    failed(new ClosedChannelException(), handler);
                } else {
                    readLocker.release();
                    try {
                        buffer.buffer.flip();
                        handler.read(buffer, result, (T) AioSocketChannel.this);
                    } catch (Exception ex) {
                        handler.failed(ex, (T) AioSocketChannel.this);
                    }
                }
            }

            @Override
            public void failed(Throwable exc, ChannelHanlder<T> handler) {
                try {
                    handler.failed(exc, (T) AioSocketChannel.this);
                } finally {
                    readLocker.release();
                }
            }
        });

    }

    @Override
    public <T extends Channel> void write(ChannelBuffer buffer, ChannelHanlder<T> handler) throws IOException {

        if (!isOpen()) {
            throw new ClosedChannelException();
        }
        try {
            if (!writeLocker.tryAcquire(5, TimeUnit.MINUTES)) {
                throw new InterruptedByTimeoutException();
            }
        } catch (InterruptedException ex) {
            throw new InterruptedByTimeoutException();
        }
        this.write0(buffer, handler);
    }

    private <T extends Channel> void write0(ChannelBuffer buffer, ChannelHanlder<T> handler) throws IOException {

        inner.write(buffer.buffer, 5, TimeUnit.SECONDS, handler, new CompletionHandler<Integer, ChannelHanlder<T>>() {
            @Override
            public void completed(Integer result, ChannelHanlder<T> handler) {

                if (result < 0) {
                    failed(new ClosedChannelException(), handler);
                } else if (buffer.buffer.hasRemaining()) {
                    try {
                        write0(buffer, handler);
                    } catch (IOException ex) {
                        failed(ex, handler);
                    }
                } else {
                    writeLocker.release();
                    try {
                        handler.written(buffer, result, (T) AioSocketChannel.this);
                    } catch (Exception ex) {
                        handler.failed(ex, (T) AioSocketChannel.this);
                    }
                }
            }

            @Override
            public void failed(Throwable exc, ChannelHanlder<T> handler) {
                try {
                    handler.failed(exc, (T) AioSocketChannel.this);
                } finally {
                    writeLocker.release();
                }
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
