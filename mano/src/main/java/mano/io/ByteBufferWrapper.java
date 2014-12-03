/*
 * Copyright (C) 2014 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.io;

import java.nio.ByteBuffer;

/**
 * 携带一个字节缓冲区的实现。
 *
 * @author junhwong
 */
public class ByteBufferWrapper implements Buffer {

    ByteBuffer buffer;

    public ByteBufferWrapper(ByteBuffer buffer) {
        this.buffer = buffer;
        //buffer.asCharBuffer();
        //java.nio.charset.Charset c=null;c.newEncoder().encode(null, buffer, true);
    }

    public ByteBufferWrapper(byte[] buffer, int offset, int length) {
        this.buffer = ByteBuffer.wrap(buffer, offset, length);
    }

    public ByteBufferWrapper(byte[] buffer) {
        this(buffer, 0, buffer.length);
    }
    
    public ByteBufferWrapper(int capacity) {
        this(ByteBuffer.allocate(capacity));
    }

    @Override
    public final boolean hasByteBuffer() {
        return buffer != null;
    }

    @Override
    public final ByteBuffer getByteBuffer() {
        return buffer;
    }

    @Override
    public final boolean isFileRegin() {
        return false;
    }

    @Override
    public final FileRegin getFileRegin() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
