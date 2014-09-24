/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.http;

import com.diosay.mano.io.ChannelBuffer;

/**
 * 表示一个HHTP消息实体解码器。
 * @author jun <jun@diosay.com>
 */
public interface HttpEntityBodyDecoder {
    /**
     * 当收到一个消息时触发。
     * @param <T>
     * @param buffer
     * @param appender 
     */
    <T extends HttpEntityBodyAppender> void onRead(ChannelBuffer buffer,T appender) throws Exception ;
}
