/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.io;

import java.nio.ByteBuffer;
import mano.Queue;
import mano.util.ArrayBlockingQueue;
import mano.util.ScheduleTask;

/**
 * @deprecated 移除
 * @author jun
 */
public class IOBufferProvider {

    private static IOBufferProvider defaultProvider = new IOBufferProvider();

    public static IOBuffer alloc() {
        return defaultProvider.allocate();
    }

    IOBufferProvider() {
        ScheduleTask.register(current -> {
            if (current - time > 5000) {
                time = current;
                int size = items.size();
                size = size / 2 + (size % 2 == 0 ? 0 : 1);
                while (size > 0) {
                    items.poll();
                    size--;
                }
            }
            return false;
        });
    }

    private long time;
    protected int bufferSize = 1024 * 4;
    protected Queue<IOBuffer> items = new ArrayBlockingQueue<>();

    protected IOBuffer allocate() {
        IOBuffer buffer = items.poll();
        if (buffer == null) {
            buffer = new PooledIOBufferImpl(ByteBuffer.allocate(bufferSize), this);
        }
        buffer.buffer().clear();

        return buffer;
    }

    protected void free(IOBuffer buffer) {
        if (buffer == null || buffer.provider() != this) {
            return;
        }
        
        if (buffer.buffer().capacity() != bufferSize || items.size()>512) {
            return;
        }

        items.offer(buffer);
    }

    static class PooledIOBufferImpl extends IOBuffer {

        private ByteBuffer buffer;
        private IOBufferProvider provider;

        public PooledIOBufferImpl(ByteBuffer buffer, IOBufferProvider provider) {
            if (buffer == null) {
                throw new NullPointerException("buffer");
            }
            if (provider == null) {
                throw new NullPointerException("provider");
            }
            this.buffer = buffer;
            this.provider = provider;
        }

        @Override
        public ByteBuffer buffer() {
            return this.buffer;
        }

        @Override
        public IOBufferProvider provider() {
            return this.provider;
        }

        @Override
        public void release() {
            if (this.provider != null) {
                this.provider.free(this);

                this.buffer = null;
                this.provider = null;
            }
        }

    }

}
