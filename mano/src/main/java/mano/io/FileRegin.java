/*
 * Copyright (C) 2014 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.io;

import java.nio.ByteBuffer;

/**
 * 表示一个文件范围。
 *
 * @author junhwong
 */
public class FileRegin implements Buffer {

    private String filename;
    private long position;
    private long length;
    
    public FileRegin(String filename,long position,long length){
        this.filename=filename;
        this.position=position;
        this.length=length;
    }

    /**
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * @return the position
     */
    public long getPosition() {
        return position;
    }

    /**
     * @return the length
     */
    public long getLength() {
        return length;
    }

    @Override
    public final boolean hasByteBuffer() {
        return false;
    }

    @Override
    public final ByteBuffer getByteBuffer() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public final boolean isFileRegin() {
        return true;
    }

    @Override
    public final FileRegin getFileRegin() {
        return this;
    }
}
