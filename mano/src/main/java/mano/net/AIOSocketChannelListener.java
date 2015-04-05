/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import mano.EventArgs;
import mano.EventHandler;
import mano.EventListener;
import mano.io.BufferManager;
import mano.io.Channel;
import mano.io.ChannelAcceptor;
import mano.io.ChannelContext;
import mano.io.ChannelException;
import mano.io.ChannelHandlerChain;
import mano.io.ChannelListenerAbstract;
import mano.io.ChannelListenerContext;
import mano.io.ChannelTask;

/**
 *
 * @author sixmoon
 */
public class AIOSocketChannelListener extends ChannelListenerAbstract {

    private AsynchronousServerSocketChannel server;
    private AsynchronousChannelGroup group;

    private ExecutorService ioExecutor;

    public AIOSocketChannelListener() {
        super();
    }

    private InetSocketAddress addr;

    @Override
    public void bind(String address, int backlog) {
        if (address.indexOf(":") > 0) {
            String[] arr = address.split(":");
            if ("".equalsIgnoreCase(arr[0])
                    || "localhost".equalsIgnoreCase(arr[0])
                    || "127.0.0.1".equalsIgnoreCase(arr[0])
                    || "0.0.0.0".equalsIgnoreCase(arr[0])) {
                addr = new InetSocketAddress(Integer.parseInt(arr[1]));
            } else {
                addr = new InetSocketAddress(arr[0], Integer.parseInt(arr[1]));
            }
        }
    }

    @Override
    protected void onStart() throws Exception {

        ioExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
        group = AsynchronousChannelGroup.withThreadPool(ioExecutor);
        server = AsynchronousServerSocketChannel.open(group);
        server.bind(addr);
        server.accept(server, new CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel>() {

            @Override
            public void completed(AsynchronousSocketChannel result, AsynchronousServerSocketChannel attachment) {

                SocketChannelImpl impl = new SocketChannelImpl();
                impl.real = result;
                acceptQueue.offer(impl);
                notifyAccept();

                attachment.accept(attachment, this);//todo
//                try (ChannelHandlerChain chain = getHandlerChain()) {
//                    try {
//                        chain.handleConnected(impl);
//                    } catch (Exception ex) {
//                        try {
//                            impl.real.close();
//                        } catch (IOException ex1) {
//                            //ex1.printStackTrace();//todo
//                        }
//                        ex.printStackTrace();
//                    }
//                }

            }

            @Override
            public void failed(Throwable exc, AsynchronousServerSocketChannel attachment) {
                exc.printStackTrace();
                try {
                    attachment.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

    }

    @Override
    protected void onStop() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isOpen() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private final Queue<ChannelContext> acceptQueue = new LinkedBlockingQueue<>();
    Runnable poller = () -> {
        synchronized (acceptQueue) {
            ChannelContext context = acceptQueue.poll();
            if (context != null) {
                try (ChannelHandlerChain chain = getHandlerChain()) {
                    try {
                        chain.handleConnected(context);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        notifyAccept();
                    }
                }
            }
        }
    };

    void notifyAccept() {
        getContext().getExecutor().execute(poller);
    }

    void start() throws IOException {

    }

    protected class SocketChannelImpl implements ChannelContext, Channel {

        private AsynchronousSocketChannel real;
        private long timeout = 1000 * 30;
        private final Queue<ByteBuffer> inboundQueue = new LinkedBlockingQueue<>();
        private final Queue<ByteBuffer> outboundQueue = new LinkedBlockingQueue<>();
        private final Queue<ChannelTask> tasks = new LinkedBlockingQueue<>();
        private volatile boolean closing;

        @Override
        public boolean hasPaddingWrite() {
            return !outboundQueue.isEmpty();
        }

        @Override
        public boolean hasPaddingRead() {
            return !inboundQueue.isEmpty();
        }

        @Override
        public final Channel channel() {
            return this;
        }

        @Override
        public final void putInbound(ByteBuffer buffer) {

        }

        private void handleError(Throwable ex) {
            try (ChannelHandlerChain chain = getHandlerChain()) {
                chain.handleError(this, ex);
            }
        }

        @Override
        public final void putOutbound(ByteBuffer buffer) {
            synchronized (outboundQueue) {
                outboundQueue.offer(buffer);
            }
            notifyWrite();
        }

        private void notifyWrite() {
            getContext().getExecutor().execute(SocketChannelImpl.this.writeFlushHandler);
        }
        private Runnable writeFlushHandler = () -> {
            if (!this.isOpen()) {
                return;
            }
            synchronized (outboundQueue) {
                ByteBuffer buffer = outboundQueue.peek();
                if (buffer != null) {
                    try {
                        real.write(buffer, timeout, TimeUnit.MILLISECONDS, buffer, SocketChannelImpl.this.writeHandler);
                        outboundQueue.poll();
                    } catch (Exception ex) {
                        //ex.printStackTrace();
                        notifyWrite();
                    }
                }
            }
        };
        private CompletionHandler<Integer, ByteBuffer> writeHandler = new CompletionHandler<Integer, ByteBuffer>() {

            @Override
            public void completed(Integer result, ByteBuffer buffer) {
                System.out.println("WRITE....." + result);
                if (result < 0) {
                    close(true);
                    failed(new IOException("remote closed by write."), buffer);
                } else {
                    if (buffer.hasRemaining()) {
                        //TODO
                        real.write(buffer, timeout, TimeUnit.MILLISECONDS, buffer, SocketChannelImpl.this.writeHandler);
                    } else {
                        free(buffer);
                        notifyWrite();
                    }
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                handleError(exc);
                free(attachment);
                notifyWrite();
            }
        };

        private void notifyRead() {
            getContext().getExecutor().execute(SocketChannelImpl.this.readFlushHandler);
        }
        private Runnable readFlushHandler = () -> {
            if (!this.isOpen()) {
                return;
            }
            synchronized (inboundQueue) {
                ByteBuffer buffer = inboundQueue.peek();
                if (buffer != null) {
                    try {
                        real.read(buffer, timeout, TimeUnit.MILLISECONDS, buffer, SocketChannelImpl.this.readHandler);
                        inboundQueue.poll();
                    } catch (Exception ex) {
                        //ex.printStackTrace();
                        notifyRead();
                    }
                }
            }
        };

        private CompletionHandler<Integer, ByteBuffer> readHandler = new CompletionHandler<Integer, ByteBuffer>() {

            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                if (result < 0) {
                    close();
                    failed(new IOException("remote closed by read."), attachment);
                } else {
                    try (ChannelHandlerChain chain = getHandlerChain()) {
                        attachment.flip();
                        chain.handleInbound(SocketChannelImpl.this, attachment);
                    }
                    notifyRead();
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                handleError(exc);
                free(attachment);
                notifyRead();
            }
        };

        @Override
        public final void queueReadBuffer(ByteBuffer buffer) {
            synchronized (inboundQueue) {
                inboundQueue.offer(buffer);
            }
            notifyRead();
        }

        @Override
        public final void queueWriteBuffer(ByteBuffer buffer) {
            try (ChannelHandlerChain chain = getHandlerChain()) {
                chain.handleOutbound(this, buffer);
            }
        }

        @Override
        public final boolean isSecure() {
            return false;
        }

        @Override
        public final boolean isOpen() {
            if (real == null || !real.isOpen()) {
                close();
                return false;
            } else {
                return true;
            }
        }

        @Override
        public void await() {
            do {
                if (this.hasPaddingRead() || this.hasPaddingWrite()) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    break;
                }
            } while (real != null && real.isOpen());

            try {
                Thread.sleep(0);//等待刷新输出
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            //System.out.println("wait....");
        }

        @Override
        public synchronized final void close(boolean force) {
            if (closing) {
                return;
            }
            closing = true;

            if (real != null && real.isOpen()) {
                try {
                    try (ChannelHandlerChain chain = getHandlerChain()) {
                        chain.handleDisconnect(this);
                    }
                    if (!force) {
                        await();
                    }
                } catch (Throwable ex) {
                    exceptionCaught(ex);
                } finally {
                    close0();
                }
            }
        }

        @Override
        public synchronized final void close() {
            if (closing) {
                return;
            }
            closing = true;
            if (real != null) {
                try {
                    try (ChannelHandlerChain chain = getHandlerChain()) {
                        chain.handleDisconnect(this);
                    }
                } catch (Throwable ex) {
                    exceptionCaught(ex);
                } finally {
                    close0();
                }
            }
        }

        private void close0() {
            this.outboundQueue.forEach(buf -> {
                free(buf);
            });
            this.inboundQueue.forEach(buf -> {
                free(buf);
            });
            this.outboundQueue.clear();
            this.inboundQueue.clear();
            try {
                real.close();
            } catch (IOException ex) {
            } finally {
                real = null;
            }
        }

        private void checkClosedThrown() throws ChannelException {
            if (!this.isOpen()) {
                throw new ChannelException("This channel was already closed.");
            }
        }

        @Override
        public final void submit(ChannelTask task) throws ChannelException {
            checkClosedThrown();

            if (task != null) {
                synchronized (tasks) {
                    tasks.offer(task);
                }
            }
            flush();

        }

        private void flush() {
            getContext().getExecutor().execute(flushHandler);
        }

        private Runnable flushHandler = () -> {
            if (!this.isOpen()) {
                return;
            }
            synchronized (tasks) {
                ChannelTask task = tasks.poll();
                if (task != null) {
                    try {
                        task.execute(this);
                        getContext().putTask(task);
                    } catch (Throwable ex) {
                        exceptionCaught(ex);
                        flush();
                    }
                }
            }
        };

        @Override
        public void read() throws ChannelException {
            submit(getContext().wrapReadTask(this.allocate()));
        }

        @Override
        public ByteBuffer allocate() {
            return getContext().getByteBuffer();
        }

        @Override
        public void free(ByteBuffer buffer) {
            getContext().putByteBuffer(buffer);
        }

        @Override
        public BufferManager getBufferManager() {
            return getContext().getBufferManager();
        }

        @Override
        public void write(CharSequence content, CharsetEncoder encoder) throws ChannelException {
            ByteBuffer buf;
            CoderResult result;
            encoder.reset();
            int len = content.length(), start = 0, end;

            do {
                buf = allocate();
                assert buf != null;
                buf.clear();
                end = buf.capacity() / (int) encoder.maxBytesPerChar(); //计算字符容量
                end = (end == 0) ? 1 : end;
                end = (end + start >= len) ? start + (len - start) : start + end;
                //System.out.println("MBP:" + encoder.maxBytesPerChar() + " start:" + start + " end:" + end);
                result = encoder.encode(java.nio.CharBuffer.wrap(content, start, end), buf, true);
                if (result.isError()) {
                    try {
                        result.throwException();
                    } catch (CharacterCodingException ex) {
                        throw new java.lang.RuntimeException(ex);
                    }
                } else if (!result.isUnderflow()) {
                    throw new java.lang.RuntimeException("Overflow");
                }

                start = end;
                buf.flip();
                write(buf);
            } while (end != len);
            encoder.reset();
        }

        @Override
        public void write(byte[] buffer) throws ChannelException {
            write(buffer, 0, buffer.length);
        }

        @Override
        public void write(byte[] buffer, int offset, int count) throws ChannelException {
            write(ByteBuffer.wrap(buffer, offset, count));
        }

        protected void write(ByteBuffer buffer) throws ChannelException {
            submit(getContext().wrapWriteTask(buffer));
        }

        /**
         * 当前信道处理过程中的异常捕获与处理。
         *
         * @param cause
         */
        protected void exceptionCaught(Throwable cause) {
            if (cause.getMessage() != null && cause.getMessage().indexOf("connection was aborted") > 0) {
                this.close();
            } else {
                //handler.handleError(this, cause);
            }
        }

        @Override
        public void copyOnPutInbound(ByteBuffer buffer) {
            ByteBuffer buf;
            do {
                buf = this.allocate();
                buf.clear();
                if (buf.capacity() >= buffer.remaining()) {
                    buf.put(buffer);
                    this.putInbound(buf);
                } else {
                    if (buffer.hasArray()) {
                        buf.put(buffer.array(), buffer.arrayOffset() + buffer.position(), buf.capacity());
                        buffer.position(buffer.position() + buf.capacity());
                        this.putInbound(buf);
                    } else {
                        while (buf.hasRemaining()) {
                            buf.put(buffer.get());
                        }
                        this.putInbound(buf);
                    }
                }
            } while (buffer.hasRemaining());
        }

        @Override
        public void copyOnPutOutbound(ByteBuffer buffer) {
            ByteBuffer buf;
            do {
                buf = this.allocate();
                buf.clear();
                if (buf.capacity() >= buffer.remaining()) {
                    buf.put(buffer);
                    this.putOutbound(buf);
                } else {
                    if (buffer.hasArray()) {
                        buf.put(buffer.array(), buffer.arrayOffset() + buffer.position(), buf.capacity());
                        buffer.position(buffer.position() + buf.capacity());
                        this.putOutbound(buf);
                    } else {
                        while (buf.hasRemaining()) {
                            buf.put(buffer.get());
                        }
                        this.putOutbound(buf);
                    }
                }
            } while (buffer.hasRemaining());
            System.out.println("done");
        }

        java.util.HashMap att = new java.util.HashMap<>();

        @Override
        public Object get(Object key) {
            if (att.containsKey(key)) {
                return att.get(key);
            }
            return null;
        }

        @Override
        public void set(Object key, Object value) {
            att.put(key, value);
        }
    }

}
