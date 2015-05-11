/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.net.http;

import java.io.IOException;

/**
 * 表示一个HHTP消息实体解码器。
 * @author jun <jun@diosay.com>
 */
public interface HttpEntityBodyDecoder {
//    /**
//     * 当收到一个消息时触发。
//     * @param <T>
//     * @param buffer
//     * @param appender 
//     */
    //<T extends HttpEntityBodyAppender> void onRead(ChannelBuffer buffer,T appender) throws Exception ;
    
    
    <T extends HttpEntityBodyAppender> void decode(T appender) throws IOException;
    
}
