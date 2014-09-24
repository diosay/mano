/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package com.diosay.mano.web;

import com.diosay.mano.io.ByteArrayMessage;
import com.diosay.mano.io.ChannelBuffer;
import com.diosay.mano.io.ChannelCloseingMessage;
import com.diosay.mano.io.ChannelHanlder;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.InterruptedByTimeoutException;
import mano.DateTime;
import mano.http.HttpException;
import mano.http.HttpHeader;
import mano.http.HttpMethod;
import mano.http.HttpStatus;
import mano.http.HttpVersion;

/**
 * HTTP 1.1协议。
 *
 * @author jun <jun@diosay.com>
 */
public class HttpProtocol11 implements ChannelHanlder<HttpChannel> {

    @Override
    public void connected(HttpChannel channel) throws Exception {
        channel.handler = this;
        channel.buffer = channel.getListener().getGroup().allocate();
        channel.start();
        channel.read(channel.buffer, this);
    }

    @Override
    public void closed(HttpChannel channel) {
        
    }

    @Override
    public void read(ChannelBuffer buffer, Integer reads, HttpChannel channel) throws Exception {
        String line = null;
        if (channel.phase == HttpChannel.LOAD_HEADER || channel.phase == HttpChannel.REQUEST_LINE) {
            HttpHeader header;
            boolean done;
            while ((line = buffer.readln(channel.getInputEncoding())) != null) {
                if (channel.phase == HttpChannel.REQUEST_LINE) {
                    channel.request = new HttpRequestImpl();
                    channel.request.channel=channel;
                    String[] arr = line.split(" ");
                    channel.request.method = HttpMethod.valueOf(arr[0]);
                    channel.request.rawUrl = arr[1];
                    channel.request.version = HttpVersion.valueOf(arr[2]);
                    channel.phase = HttpChannel.LOAD_HEADER;
                } else if ("".equals(line)) {
                    channel.phase = HttpChannel.RESPONSE;
                    if (!channel.postResponse()) {
                        throw new HttpException(HttpStatus.BadRequest, "Bad Request(Invalid Hostname)");
                    }
                    return;
                } else {
                    header = HttpHeader.prase(line);
                    channel.request.headers.put(header);
                }
            }
            if (!buffer.buffer.hasRemaining()) {
                failed(new IllegalArgumentException("缓冲区已满，请求头太大"), channel);
            } else {
                channel.read(buffer, this);
            }
        } else if (channel.phase == HttpChannel.RESPONSE) {
            if (channel.request == null || channel.request.decoder == null) {
                throw new IllegalStateException("未设置处理程序");
            }
            channel.request.decoder.onRead(buffer, channel.request);
            if (channel.request.remaining > 0) {
                if (!buffer.hasRemaining()) {
                    buffer.clear();
                } else {
                    buffer.compact();
                }
                channel.read(buffer, this);
            }
        } else {
            //error
            throw new IllegalStateException("未定义的处理阶段");
        }

    }

    @Override
    public void written(ChannelBuffer buffer, Integer reads, HttpChannel channel) {
    }

    @Override
    public void failed(Throwable exc, HttpChannel channel) {

        if (exc instanceof InterruptedByTimeoutException) {
            channel.close();
            return;
        } else if (exc instanceof ClosedChannelException) {
            channel.close();
            return;
        }

        if (exc instanceof HttpException) {
            this.responseError((HttpException) exc, channel);
        } else {
            System.err.println("error=======================");
            exc.printStackTrace(System.err);
            this.responseError(new HttpException(HttpStatus.InternalServerError, exc), channel);
        }
    }

    public void responseError(HttpException ex, HttpChannel channel) {

        byte[] response = String.format("<html><head><title>HTTP %s Error</title></head><body>%s</body></html>", ex.getHttpCode(), ex.getMessage()).getBytes();
        StringBuilder sb = new StringBuilder("HTTP/1.1 ");
        sb.append(ex.getHttpCode()).append(" ").append(HttpStatus.getKnowDescription(ex.getHttpCode())).append("\r\n");
        sb.append("Content-Length:").append(response.length).append("\r\n");
        sb.append("Connection:close").append("\r\n");
        sb.append("Date:").append(DateTime.now().toGMTString()).append("\r\n");
        sb.append("\r\n");

        byte[] bytes = sb.toString().getBytes(channel.getInputEncoding());
        ByteArrayMessage msg = new ByteArrayMessage();
        msg.handler = this;
        msg.array = bytes;
        msg.offset = 0;
        msg.length = bytes.length;
        channel.enqueue(msg);

        msg = new ByteArrayMessage();
        msg.handler = this;
        msg.array = response;
        msg.offset = 0;
        msg.length = response.length;
        channel.enqueue(msg);

        ChannelCloseingMessage msg2 = new ChannelCloseingMessage();
        msg2.handler = this;
        channel.enqueue(msg2);

    }

}
