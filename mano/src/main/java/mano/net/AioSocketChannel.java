/*
 * Copyright (C) 2014 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.net;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.nio.channels.WritableByteChannel;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import mano.data.Pageable;
import mano.io.Buffer;
import mano.io.channel.Channel;
import mano.io.channel.Hanlder;
import mano.io.channel.Listener;

/**
 * 异步Socket通道的实现。
 *
 * @author junhwong
 */
public class AioSocketChannel implements SocketChannel {

    @Override
    public void read(Buffer message) {
        PaddingItem item = new PaddingItem();
        item.action = PaddingItem.READ;
        item.buffer = message;
        paddings.offer(item);
    }

    @Override
    public void write(Buffer message) {
        PaddingItem item = new PaddingItem();
        item.action = PaddingItem.WRITE;
        item.buffer = message;
        paddings.offer(item);
    }

    @Override
    public void close() {
        if (isOpen()) {
            close0();
            //handle
        }
    }

    class PaddingItem {

        public static final String WRITE = "write";
        public static final String READ = "read";
        public static final String PROMISE = "promise";
        public String action;
        public Buffer buffer;
        public Object state;

        public void reset() {
            action = null;
            buffer = null;
            state = null;
        }
    }

    private class WriteProxy implements WritableByteChannel {

        @Override
        public int write(ByteBuffer src) throws IOException {
            Future<Integer> future = real.write(src);
            try {
                return future.get(getTimeout(), TimeUnit.MILLISECONDS);
            } catch (Throwable ex) {
                throw new IOException(ex);
            }
        }

        @Override
        public boolean isOpen() {
            return AioSocketChannel.this.isOpen();
        }

        @Override
        public void close() throws IOException {
            close0();
        }

    }

    protected AsynchronousSocketChannel real;
    protected final Lock locker = new ReentrantLock();
    //protected final Semaphore locker = new Semaphore(1);
    protected final Queue<PaddingItem> paddings = new LinkedBlockingQueue<>();
    private WriteProxy proxy = new WriteProxy();
    private ExecutorService executor;

    public AioSocketChannel(AsynchronousSocketChannel channel, ExecutorService executor) {
        this.real = channel;
        this.executor = executor;
    }

    @Override
    public Listener getListener() {

        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SocketAddress getLocalAddress() throws IOException {
        return real.getLocalAddress();
    }

    @Override
    public SocketAddress getRemoteAddress() throws IOException {
        return real.getRemoteAddress();
    }

    @Override
    public <T> AioSocketChannel setOption(SocketOption<T> name, T value) throws IOException {
        real.setOption(name, value);
        return this;
    }

    @Override
    public <T> T getOption(SocketOption<T> name) throws IOException {
        return real.getOption(name);
    }

    public long getTimeout() {
        return 5000;//5s
    }

    <T extends Channel, B extends Buffer> Hanlder<T, B> getHanlder() {
        Class<? extends Hanlder<? extends Channel, ? extends Buffer>> g = null;
        //Hanlder h=g.newInstance();
        return null;
    }

    void putHanlder(Hanlder handler) {

    }

    void uncaught(Throwable exception, Object attachment) {
        exception.printStackTrace(System.err);
    }

    void handleRead(Integer result, Buffer buffer) {
        try {
            Hanlder handler = getHanlder();
            try {
                handler.onRead(this, buffer);
            } catch (Throwable inner) {
                handler.onFailed(this, inner, buffer);
            } finally {
                putHanlder(handler);
            }
        } catch (Throwable ex) {
            uncaught(ex, buffer);
        }
    }

    void handleWritten(Long result, Buffer buffer) {
        try {
            Hanlder handler = getHanlder();
            try {
                handler.onRead(this, buffer);
            } catch (Throwable inner) {
                handler.onFailed(this, inner, buffer);
            } finally {
                putHanlder(handler);
            }
        } catch (Throwable ex) {
            uncaught(ex, buffer);
        }
    }

    protected void handleFailed(Throwable exception, Object attachment) {
        try {
            Hanlder handler = getHanlder();
            try {
                handler.onFailed(this, exception, attachment);
            } finally {
                putHanlder(handler);
            }
        } catch (Throwable ex) {
            uncaught(ex, attachment);
        }
    }

    protected void handlePromised(Object attachment) {
        try {
            Hanlder handler = getHanlder();
            try {
                handler.onPromised(this);
            } finally {
                putHanlder(handler);
            }
        } catch (Throwable ex) {
            uncaught(ex, attachment);
        }
    }

    protected void close0() {
        try {
            if (real != null) {
                real.close();
            }
        } catch (Throwable e) {
        } finally {
            real = null;
        }
    }

    /**
     * 执行挂起的任务。
     */
    protected void flush() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (!isOpen()) {
                    return;
                }
                try {
                    if (!locker.tryLock(getTimeout(), TimeUnit.MILLISECONDS)) {
                        throw new IllegalStateException("failed to acquiring to flush Locker.");
                    }
                } catch (Throwable ex) {
                    uncaught(ex, null);
                    return;
                }

                PaddingItem item = paddings.poll();
                try {

                    if (item != null) {
                        switch (item.action) {
                            case PaddingItem.READ:
                                try {
                                    read0(item.buffer);
                                } catch (Throwable ex) {
                                    handleFailed(ex, item.buffer);
                                }
                                break;
                            case PaddingItem.WRITE:
                                try {
                                    write0(item.buffer);
                                } catch (Throwable ex) {
                                    handleFailed(ex, item.buffer);
                                }
                                break;
                            case PaddingItem.PROMISE:
                                try {
                                    handlePromised(item.state);
                                } catch (Throwable ex) {
                                    handleFailed(ex, item);
                                } finally {
                                    locker.unlock();
                                }
                                break;
                        }
                    }
                } catch (Throwable ex) {
                    locker.unlock();
                    uncaught(ex, null);
                } finally {
                    //put item?
                }
            }
        });
    }

    @Override
    public boolean isOpen() {
        return real != null && real.isOpen();
    }

    /**
     * 从通道中读取数据到缓冲区。
     *
     * @param message
     * @throws ClosedChannelException
     */
    protected void read0(Buffer message) throws ClosedChannelException {
        if (message == null || !message.hasByteBuffer() || message.getByteBuffer() == null) {
            throw new IllegalArgumentException("Read opertion must be has ByteBuffer.");
        }
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
        real.read(message.getByteBuffer(), getTimeout(), TimeUnit.MILLISECONDS, message, new CompletionHandler<Integer, Buffer>() {

            @Override
            public void completed(Integer result, Buffer attachment) {
                if (result < 0) {
                    close0();
                    handleFailed(new ClosedChannelException(), attachment);
                } else {
                    attachment.getByteBuffer().flip();//确定数据。
                    handleRead(result, attachment);
                    locker.unlock();
                }
            }

            @Override
            public void failed(Throwable exc, Buffer attachment) {
                handleFailed(exc, attachment);
                locker.unlock();
            }
        });
    }

    /**
     * 将缓冲区的数据异步写入通道。
     *
     * @param message
     * @throws ClosedChannelException
     */
    protected void write0(Buffer message) throws ClosedChannelException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
        if (message == null || !message.hasByteBuffer() || message.getByteBuffer() == null) {
            throw new NullPointerException("message");
        } else if (message.isFileRegin()) {
            try {
                long result = 0;
                try (FileInputStream in = new FileInputStream(message.getFileRegin().getFilename())) {
                    result = in.getChannel().transferTo(message.getFileRegin().getPosition(), message.getFileRegin().getLength(), proxy);
                }
                handleWritten(result, message);
            } catch (Throwable ex) {
                handleFailed(ex, message);
            } finally {
                locker.unlock();
            }
        } else if (message.hasByteBuffer()) {
            real.write(message.getByteBuffer(), getTimeout(), TimeUnit.MILLISECONDS, message, new CompletionHandler<Integer, Buffer>() {

                @Override
                public void completed(Integer result, Buffer attachment) {
                    if (result < 0) {
                        close0();
                        handleFailed(new ClosedChannelException(), attachment);
                    } else if (message.getByteBuffer().hasRemaining()) {
                        try {
                            write0(attachment);
                        } catch (Throwable ex) {
                            handleFailed(ex, attachment);
                            locker.unlock();
                        }
                    } else {
                        handleRead(result, attachment);
                        locker.unlock();
                    }
                }

                @Override
                public void failed(Throwable exc, Buffer attachment) {
                    handleFailed(exc, attachment);
                    locker.unlock();
                }
            });
        } else {
            throw new IllegalArgumentException("message");
        }
    }

    public static void main(String[] args) throws IOException {

        ExecutorService executor = Executors.newCachedThreadPool();
        AsynchronousChannelGroup group = AsynchronousChannelGroup.withThreadPool(executor);
        AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open(group);
        server.bind(new InetSocketAddress(8899), 12);
        server.accept(server, new CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel>() {

            @Override
            public void completed(AsynchronousSocketChannel result, AsynchronousServerSocketChannel attachment) {
                Channel channel = new AioSocketChannel(result, executor);
                Pageable p;
            }

            @Override
            public void failed(Throwable exc, AsynchronousServerSocketChannel attachment) {
            }
        });

    }

}
