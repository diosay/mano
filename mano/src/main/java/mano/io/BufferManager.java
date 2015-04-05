/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.io;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;
import mano.util.ThreadPool;

/**
 *
 * @author sixmoon
 */
public class BufferManager {

    int bufferSize = 1024 * 4;
    int bufferLargeSize = 1024 * 17;
    private final ReentrantLock lock = new ReentrantLock();
    private final ArrayList<ByteBuffer> buffers = new ArrayList<>();
    private final ArrayList<ByteBuffer> largeBuffers = new ArrayList<>();
    private final Runnable runner;
    private volatile boolean destoried;

    public BufferManager() {
        runner = () -> {
            lock.lock();
            try {
                int size = buffers.size();
                size = size / 2 + (size % 2 == 0 ? 0 : 1);
                while (size > 0) {
                    buffers.remove(0);
                    size--;
                }

                size = largeBuffers.size();
                size = size / 2 + (size % 2 == 0 ? 0 : 1);
                while (size > 0) {
                    largeBuffers.remove(0);
                    size--;
                }
            } finally {
                lock.unlock();
            }
        };
        ThreadPool.addScheduledTask(runner);
    }

    public ByteBuffer alloc() throws IllegalMonitorStateException{
        if(destoried){
            throw new IllegalMonitorStateException("This Manager(instance) has been destoried.");
        }
        ByteBuffer buf;
        lock.lock();
        try {
            if (buffers.isEmpty()) {
                buf = ByteBuffer.allocate(bufferSize);
            } else {
                buf = buffers.get(0);
                buffers.remove(0);
            }
        } finally {
            lock.unlock();
        }
        return buf;
    }

    public ByteBuffer allocLarge() throws IllegalMonitorStateException{
        if(destoried){
            throw new IllegalMonitorStateException("This Manager(instance) has been destoried.");
        }
        ByteBuffer buf;
        lock.lock();
        try {
            if (largeBuffers.isEmpty()) {
                buf = ByteBuffer.allocate(bufferLargeSize);
            } else {
                buf = largeBuffers.get(0);
                largeBuffers.remove(0);
            }
        } finally {
            lock.unlock();
        }
        return buf;
    }

    /**
     * 将给定缓冲区中的数据合并到一个新的缓冲区中，并将给定缓冲释放。
     *
     * @param buffers 要合并的缓冲区。
     * @return 新的缓冲区。
     * @throws IllegalArgumentException 合并失败。
     */
    public ByteBuffer merge(ByteBuffer... buffers) throws IllegalArgumentException,IllegalMonitorStateException {
        int size = 0;
        for (ByteBuffer buf : buffers) {
            size += buf.remaining();
        }
        if (size == 0) {
            throw new java.lang.IllegalArgumentException("给定缓冲区中没有数据。");
        } else if (bufferSize < size && size <= bufferLargeSize) {
            ByteBuffer buffer = allocLarge();
            buffer.clear();
            for (ByteBuffer buf : buffers) {
                buffer.put(buf);
                free(buf);
            }
            return buffer;
        } else if (bufferSize >= size) {
            ByteBuffer buffer = alloc();
            buffer.clear();
            for (ByteBuffer buf : buffers) {
                buffer.put(buf);
                free(buf);
            }
            return buffer;
        } else {
            throw new java.lang.IllegalArgumentException("给定缓冲区太大，不能完成合并");
        }
    }

    public boolean free(ByteBuffer buffer) {
        if (destoried || buffer == null || buffer.isDirect()) {
            return false;
        } else if (buffer.capacity() == bufferSize) {
            lock.lock();
            try {
                buffers.add(buffer);
            } finally {
                lock.unlock();
            }
            return true;
        } else if (buffer.capacity() == bufferLargeSize) {
            lock.lock();
            try {
                largeBuffers.add(buffer);
            } finally {
                lock.unlock();
            }
            return true;
        } else {
            return false;
        }

//        java.io.ByteArrayInputStream is=new java.io.ByteArrayInputStream(null);
//        java.io.ByteArrayOutputStream out;
//        java.io.BufferedReader r;
    }

    public void destory() {
        if(destoried){
            return;
        }
        lock.lock();
        try {
            buffers.clear();
            largeBuffers.clear();
        } finally {
            destoried=true;
            lock.unlock();
        }
    }

}
