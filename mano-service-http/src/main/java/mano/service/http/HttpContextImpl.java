/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.service.http;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import mano.io.BufferManager;
import mano.io.BufferUtil;
import mano.io.Channel;
import mano.io.ChannelException;
import mano.net.http.HttpContext;
import mano.net.http.HttpHeader;
import mano.net.http.HttpMethod;
import mano.net.http.HttpRequest;
import mano.net.http.HttpResponse;
import mano.net.http.HttpServer;
import mano.net.http.HttpVersion;
import mano.web.HttpSession;
import mano.web.WebApplication;

/**
 *
 * @author sixmoon
 */
public class HttpContextImpl implements HttpContext {
    
    private HttpRequestImpl request;
    private HttpResponseImpl response;
    
    public HttpContextImpl() {
        request = new HttpRequestImpl();
        response = new HttpResponseImpl();
    }
    
    @Override
    public WebApplication getApplication() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public boolean isCompleted() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public HttpRequest getRequest() {
        return request;
    }
    
    @Override
    public HttpResponse getResponse() {
        return response;
    }
    
    @Override
    public HttpServer getServer() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public HttpSession getSession() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private Channel channel;
    private int step;
    private final int STEP_REQUEST_LINE = 0;
    private final int STEP_HEADERS = 1;
    
    public void setChannel(Channel channel) {
        this.channel = channel;
    }
    private byte[] CRLF = "\r\n".getBytes();
    private ByteBuffer keep_buffer;
    
    public void onRead(ByteBuffer buffer, BufferManager bufferManager) throws ChannelException {
        if (keep_buffer != null) {
            buffer = bufferManager.merge(keep_buffer, buffer);
            keep_buffer = null;
            buffer.flip();
        }
        int index, len, pos;
        String line;
        HttpHeader header;
        while (buffer.hasRemaining()) {
            if (step == STEP_REQUEST_LINE) {
                index = BufferUtil.bytesIndexOf(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining(), BufferUtil.CRLF);
                if (index >= 0) {
                    len = index - (buffer.arrayOffset() + buffer.position());
                    line = new String(buffer.array(), buffer.arrayOffset() + buffer.position(), len);
                    System.out.println("LINE:" + line + "///>");
                    buffer.position(buffer.position() + len + BufferUtil.CRLF.length);
                    
                    String[] arr = mano.util.Utility.split(line, " ", true);
                    //get / http/1.1
                    if (arr.length != 3) {
                        //throw 错误的请求行
                    }
                    request.method = HttpMethod.parse(arr[0]);
                    request.rawUrl = arr[1];
                    request.version = HttpVersion.valueOf(arr[2]);
                    step = STEP_HEADERS;
                } else {
                    keep_buffer = buffer;
                    channel.read();
                }
            } else if (step == STEP_HEADERS) {
                index = BufferUtil.bytesIndexOf(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining(), BufferUtil.CRLF);
                if (index >= 0) {
                    len = index - (buffer.arrayOffset() + buffer.position());
                    line = new String(buffer.array(), buffer.arrayOffset() + buffer.position(), len);
                    buffer.position(buffer.position() + len + BufferUtil.CRLF.length);
                    System.out.println("LINE:" + line + "///>");
                    if (line.length() == 0) {
                        System.out.println("OK");
                        if (buffer.hasRemaining()) {
                            keep_buffer = buffer;
                        } else {
                            bufferManager.free(buffer);
                        }
                        
                        channel.write("hello", Charset.forName("utf-8").newEncoder());
                        channel.submit((x) -> {
                            x.channel().await();
                            x.channel().close();
                        });
                    } else {
                        header = HttpHeader.prase(line);
                        if (header == null) {
                            //throw 错误的头信息
                            System.out.println("EER:" + line);
                            
                        }
                        request.headers.put(header);
                    }
                } else {
                    keep_buffer = buffer;
                    channel.read();
                }
            }
        }
    }
    
}
