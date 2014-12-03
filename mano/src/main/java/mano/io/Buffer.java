/*
 * Copyright (C) 2014 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.io;

import java.nio.ByteBuffer;

/**
 *
 * @author junhwong
 */
public interface Buffer {
    boolean hasByteBuffer();
    ByteBuffer getByteBuffer();
    boolean isFileRegin();
    FileRegin getFileRegin();
}
