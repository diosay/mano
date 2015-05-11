/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.io;


/**
 * @deprecated 移除
 * 表示一个文件区域。
 * @author johnwhang
 */
public class FileRegion {
    
    private long offset;
    private long length;
    private String filename;
    
    public FileRegion(String filename,long offset,long length){
        this.filename=filename;
        this.offset=offset;
        this.length=length;
    }
    
    /**
     * 获取文件的起始偏移位置。
     */
    public long getOffset(){
        return this.offset;
    }
    /**
     * 获取要传输的长度。
     */
    public long getLength(){
        return this.length;
    }
    /**
     * 获取文件的绝对文路径。
     */
    public String getFilename(){
        return this.filename;
    }
    //ReadableByteChannel getReadableChannel();
    //WritableByteChannel getWritableChannel();
}
