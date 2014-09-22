/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import mano.util.ThreadPool;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class ChannelAbstract implements IChannel {

    AsynchronousSocketChannel inner;
    protected final Queue<Message> paddings = new LinkedBlockingQueue<>();
    protected Semaphore writeLocker = new Semaphore(1);

    @Override
    public void enqueue(Message message) {
        paddings.offer(message);
        flush();
    }

    @Override
    public <T> void read(ByteBuffer buffer, T attachment) throws IOException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
        inner.read(buffer, 5, TimeUnit.SECONDS, attachment, new CompletionHandler<Integer, T>() {
            @Override
            public void completed(Integer result, T attachment) {
                try {
                    if (result < 0) {
                        failed(new ClosedChannelException(), attachment);
                    } else {
                        onWrited(result, attachment);
                    }
                } finally {
                    writeLocker.release();
                }
            }

            @Override
            public void failed(Throwable exc, T attachment) {
                try {
                    onFailed(exc, attachment);
                } finally {
                    writeLocker.release();
                }
            }
        });
    }

    protected void flush() {
        ThreadPool.execute(new Runnable() {

            @Override
            public void run() {
                synchronized (paddings) {
                    if (paddings.isEmpty()) {
                        return;
                    }

                    try {
                        writeLocker.acquire();
                    } catch (InterruptedException ex) {
                        return;
                    }
                    Message msg = paddings.poll();
                    if (msg != null) {
                        try {
                            msg.process(ChannelAbstract.this, msg);
                        } catch (IOException ex) {
                            onFailed(ex, msg);
                        }
                        onFlushed(msg);
                        msg = null;
                    }
                }
            }
        });
    }

    protected <T> void onWrited(Integer count, T attachment) {

    }

    protected <T> void onFailed(Throwable exc, T attachment) {

    }

    protected void onFlushed(Message msg) {
    }

    ;

    @Override
    public <T> void write(ByteBuffer buffer, T attachment) throws IOException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
        inner.write(buffer, 5, TimeUnit.SECONDS, attachment, new CompletionHandler<Integer, T>() {
            @Override
            public void completed(Integer result, T attachment) {
                try {
                    if (result < 0) {
                        failed(new ClosedChannelException(), attachment);
                    } else {
                        onWrited(result, attachment);
                    }
                } finally {
                    writeLocker.release();
                }
            }

            @Override
            public void failed(Throwable exc, T attachment) {
                try {
                    onFailed(exc, attachment);
                } finally {
                    writeLocker.release();
                }
            }
        });
    }

    @Override
    public <T> void write(String filename, long position, long length, T attachment) {
        throw new UnsupportedOperationException("Not supported yet.");
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

    class Msg implements Message {

        String text;

        @Override
        public <T> void process(IChannel channel, T attachment) throws IOException {
            channel.write(ByteBuffer.wrap(text.getBytes()), attachment);

        }

        @Override
        public void reset() {
            text = null;
        }

    }
}
