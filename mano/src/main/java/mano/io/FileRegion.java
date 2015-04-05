/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.io;

import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * 表示一个文件区域。
 * @author johnwhang
 */
public interface FileRegion {
    /**
     * 获取文件的起始偏移位置。
     */
    long getOffset();
    /**
     * 获取要传输的长度。
     */
    long getLength();
    /**
     * 获取文件的绝对文路径。
     */
    String getFilename();
    //ReadableByteChannel getReadableChannel();
    //WritableByteChannel getWritableChannel();
}
