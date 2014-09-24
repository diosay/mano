/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

import com.diosay.mano.io.ChannelBuffer;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * 表单解码器。
 *
 * @author jun <jun@diosay.com>
 */
public class HttpFormUrlEncodedDecoder implements HttpEntityBodyDecoder {

    ChannelBuffer temp;
    String line;
    boolean done;

    @Override
    public <T extends HttpEntityBodyAppender> void onRead(ChannelBuffer buffer, T appender) {
        if (done) {
            buffer.position(buffer.limit() - 1);
            return;
        }
        if (buffer.capacity() >= appender.getContentLength()) {
            line = buffer.readln(appender.getEncoding());
            if (line != null) {
                temp=null;
                done=true;
                HashMap<String, String> map = new HashMap<>();
                HttpUtil.queryStringToMap(line, map);
                line=null;
                map.entrySet().forEach(item -> {
                    appender.appendFormItem(item.getKey(), item.getValue());
                });
                appender.notifyDone();
                
            }
        } else {
            if (temp == null) {
                temp = new ChannelBuffer((int) appender.getContentLength());
            }
            buffer.buffer.get(temp.array(), temp.offset() + temp.position(), buffer.buffer.remaining());
            line = temp.readln(appender.getEncoding());
            if (line != null) {
                temp=null;
                done=true;
                HashMap<String, String> map = new HashMap<>();
                HttpUtil.queryStringToMap(line, map);
                line=null;
                map.entrySet().forEach(item -> {
                    appender.appendFormItem(item.getKey(), item.getValue());
                });
                appender.notifyDone();
                
            }
        }
    }

}
