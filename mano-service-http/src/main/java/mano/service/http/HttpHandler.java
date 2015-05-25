/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.service.http;

import java.io.IOException;
import java.nio.ByteBuffer;
import mano.io.BufferUtil;
import mano.io.ChannelContext;
import mano.io.ChannelException;
import mano.io.ChannelHandler;
import mano.io.ChannelHandlerChain;
import mano.net.http.HttpMethod;
import mano.net.http.HttpVersion;
import mano.service.http.HttpListener.HttpContextImpl;

/**
 *
 * @author sixmoon
 */
public class HttpHandler implements ChannelHandler {

    @Override
    public void setProperty(String property, Object value) {
    }

    @Override
    public void init() throws Exception {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void handleOpened(ChannelContext context, ChannelHandlerChain chain) {
        try {
            context.channel().read();
        } catch (ChannelException ex) {
            context.handleError(ex);
        }
    }

    @Override
    public void handleClosed(ChannelContext context, ChannelHandlerChain chain) {
    }

    @Override
    public void handleInbound(ChannelContext context, ChannelHandlerChain chain, ByteBuffer buffer) {
        HttpContextImpl ctx = (HttpContextImpl) context;
        if(ctx==null){
            //TODO
        }
        if (ctx.step == HttpContextImpl.STEP_REQUEST_LINE || ctx.step == HttpContextImpl.STEP_HEADERS) {

            //long a=System.currentTimeMillis();
            byte[] array;
            int offset;
            int length;
            if (buffer.hasArray()) {
                array = buffer.array();
                offset = buffer.arrayOffset() + buffer.position();
                length = buffer.remaining();
            } else {
                length = buffer.remaining();
                offset = 0;
                array = new byte[length];
                buffer.get(array);
                buffer.position(buffer.position() - length);
            }

            int index;
            int len;
            String line;
            //long b=System.currentTimeMillis();
            do {
                index = mano.io.BufferUtil.bytesIndexOf(array, offset, length, BufferUtil.CRLF);
                if (index >= 0) {
                    len = index - offset;
                    line = new String(array, offset, len);
                    len+=BufferUtil.CRLF.length;
                    buffer.position(buffer.position() + len);
                    length-=len;
                    offset+=len;
                    if (ctx.step == HttpContextImpl.STEP_REQUEST_LINE) {
                        String[] arr = line.split(" ");
                        ctx.request.method= HttpMethod.parse(arr[0]);
                        ctx.request.rawUrl=arr[1];
                        ctx.request.version = HttpVersion.valueOf(arr[2]);
                        ctx.step = HttpContextImpl.STEP_HEADERS;
                    } else if (ctx.step == HttpContextImpl.STEP_HEADERS && "".equals(line)) {
                        ctx.step = HttpContextImpl.STEP_PROC;
                        if (buffer.hasRemaining()) {
                            ctx.keepBuffer = buffer;
                        } else {
                            context.freeBuffer(buffer);
                        }
                        //long c=System.currentTimeMillis();
                        ctx.handleRequest();
                        //long d=System.currentTimeMillis();
                        //System.out.println("a:"+a+" b:"+b+" c:"+c+" d:"+d);
                        break;
                    } else {
                        ctx.request.headers.put(mano.net.http.HttpHeader.prase(line));
                    }
                } else {
                    try {
                        if (buffer.hasRemaining()) {
                            buffer.compact();
                            if (!buffer.hasRemaining()) {
                                context.handleError(new java.lang.IndexOutOfBoundsException("超出緩衝區"));
                                return;
                            }
                        } else {
                            buffer.clear();
                        }
                        context.channel().read(buffer);
                    } catch (ChannelException ex) {
                        context.handleError(ex);
                    }
                    break;
                }
            } while (true);
        } else {
            //TODO
            ctx.request.writeEntityBody(buffer);
        }
    }

    @Override
    public void handleOutbound(ChannelContext context, ChannelHandlerChain chain, ByteBuffer buffer) {
        try {
            while (buffer.hasRemaining()) {
                if(context.send(buffer)<0){
                    throw new IOException("远程客户端连接中断。");//An established connection was aborted by the software in your host machine
                }
            }
        } catch (IOException ex) {
            context.handleError(ex);
        }

        context.freeBuffer(buffer);
    }

}
