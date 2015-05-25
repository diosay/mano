/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.net.http;

import java.io.IOException;
import java.util.HashMap;

/**
 * 表单解码器。
 *
 * @author jun <jun@diosay.com>
 */
public class HttpFormUrlEncodedDecoder implements HttpEntityBodyDecoder {

//    ChannelBuffer temp;
//    String line;
//    boolean done;
//    long length;
//    java.io.OutputStream stream;
//
//    private <T extends HttpEntityBodyAppender> void line(ChannelBuffer buffer, T appender) {
//        line = buffer.readln(appender.getEncoding());
//        if (line == null && buffer.remaining() == appender.getContentLength()) {
//            line = new String(buffer.array(), buffer.offset() + buffer.position(), buffer.remaining(), appender.getEncoding());
//            buffer.position(buffer.limit());
//        }
//        if (line != null) {
//            temp = null;
//            done = true;
//            HashMap<String, String> map = new HashMap<>();
//            HttpUtil.queryStringToMap(line, map, appender.getEncoding());
//            line = null;
//            map.entrySet().forEach(item -> {
//                appender.appendFormItem(item.getKey(), item.getValue());
//            });
//            appender.notifyDone();
//
//        }
//    }
//
//    @Override
//    public <T extends HttpEntityBodyAppender> void onRead(ChannelBuffer buffer, T appender) {
//        if (done) {
//            buffer.position(buffer.limit());//TODO//
//            return;
//        }
//        if (buffer.capacity() >= appender.getContentLength()) {
//            line(buffer, appender);
//        } else {
//            if (temp == null) {
//                temp = new ChannelBuffer((int) appender.getContentLength());
//            }
//            buffer.buffer.get(temp.array(), temp.offset() + temp.position(), buffer.buffer.remaining());
//            line(temp, appender);
//        }
//    }
//
//    private BufferedReader getReader(OutputStream out, Charset c) throws IOException {
//        InputStreamReader ir;
//        if (out instanceof ByteArrayOutputStream) {
//            ir = new InputStreamReader(new ByteArrayInputStream(((ByteArrayOutputStream) out).toByteArray()), c);
//        } else if (out instanceof FileOutputStream) {
//            ir = new InputStreamReader(new FileInputStream(((FileOutputStream) out).getFD()), c);
//        } else {
//            throw new java.lang.UnsupportedOperationException("不支持流类型：" + out);
//        }
//
//        return new BufferedReader(ir);
//    }

    public <T extends HttpEntityBodyAppender> void decode(T appender) throws IOException {

        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(appender.getEntityBodyStream(), appender.getEncoding()))) {
            String line = reader.readLine();
            if (line != null) {
                HashMap<String, String> map = new HashMap<>();
                HttpUtil.queryStringToMap(line, map, appender.getEncoding());
                
                map.entrySet().forEach(item -> {
                    appender.appendFormItem(item.getKey(), item.getValue());
                });
                appender.notifyDone();
            }
        }
    }

}
