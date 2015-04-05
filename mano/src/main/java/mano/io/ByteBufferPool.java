/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.io;

import java.nio.ByteBuffer;
import mano.util.CachedObjectRecyler;

/**
 * 表示一个缓冲区池。
 *
 * @author sixmoon
 */
public class ByteBufferPool extends CachedObjectRecyler<ByteBuffer> {

    private int bufferSize;

    /**
     * 使用默认缓冲区大小(4096字节)初始化。
     */
    public ByteBufferPool() {
        this(4096);
    }

    /**
     * 使用指定缓冲区大小初始化。
     *
     * @param bufferSize 缓冲区大小(字节)。
     * @throws IllegalArgumentException 缓冲区大小必须介于1与{@link Integer#MAX_VALUE}之间。
     */
    public ByteBufferPool(int bufferSize) {
        super();
        if (bufferSize <= 0 || bufferSize > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("bufferSize mast be between 1 and Integer.MAX_VALUE.");
        }
        this.bufferSize = bufferSize;
    }

    /**
     * 获取当前池的缓冲区大小。
     */
    public int getBufferSize() {
        return bufferSize;
    }

    @Override
    protected ByteBuffer createNew() {
        return ByteBuffer.allocate(bufferSize);
    }

    @Override
    public boolean put(ByteBuffer buffer) {
        if (buffer == null || buffer.capacity() != bufferSize || buffer.isDirect()) {
            return false;
        }
        buffer.clear();
        return super.put(buffer);
    }

}
