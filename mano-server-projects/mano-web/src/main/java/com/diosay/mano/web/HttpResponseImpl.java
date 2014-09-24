/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package com.diosay.mano.web;

import com.diosay.mano.io.ByteArrayMessage;
import com.diosay.mano.io.ByteBufferMessage;
import com.diosay.mano.io.Channel;
import com.diosay.mano.io.ChannelBuffer;
import com.diosay.mano.io.ChannelHanlder;
import com.diosay.mano.io.FileReginMessage;
import com.diosay.mano.io.Message;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;
import mano.DateTime;
import mano.InvalidOperationException;
import mano.http.HttpCookieCollection;
import mano.http.HttpHeader;
import mano.http.HttpHeaderCollection;
import mano.http.HttpResponse;
import mano.net.DBuffer;

/**
 * 当前HTTP请求的响应。
 *
 * @author jun <jun@diosay.com>
 */
public class HttpResponseImpl extends HttpResponse {

    HttpChannel channel;
    ByteBuffer buffer;
    boolean headerSent = false;
    boolean chunked = false;
    boolean endFlush = false;
    long contentLength = 0;
    boolean auto = true;
    static final String CRLF = "\r\n";
    HttpHeaderCollection headers = new HttpHeaderCollection();

    private void writeHeaders() {
        checkAndThrowHeaderSent();
        

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s %s %s%s", "HTTP/1.1", this.status(), this.statusDescription(), CRLF));

        if (!headers.containsKey("Date")) {
            this.setHeader("Date", DateTime.now().toGMTString());
        }

        if (!headers.containsKey("Connection")) {
            this.setHeader("Connection", channel.request.keepAlive() ? "keep-alive" : "close");
        }

        if (!headers.containsKey("Content-Type")) {
            this.setHeader("Content-Type", "text/html;charset=utf-8");
        }

        if (!this.chunked || this.endFlush) {
            this.setHeader("Content-Length", this.contentLength + "");
        } else {
            this.setHeader("Transfer-Encoding", "chunked");
        }

        /*if (connection.context != null) {
         if (connection.context.session != null && connection.context.session.isNewSession()) {
         this.getCookie().set(HttpSession.COOKIE_KEY, connection.context.session.getSessionId(), 0, null, null, true, false);
         }
         this.setHeader("Server", connection.context.getServer().getVersion());
         this.setHeader("X-Powered-By", "mano/1.1,java/1.8");
         }*/
        for (Map.Entry<String, HttpHeader> entry : headers.entrySet()) {
            sb.append(String.format("%s%s", entry.getValue().toString(), CRLF));
        }

        for (HttpCookieCollection.CookieEntry entry : this.getCookie().iterator()) {
            sb.append(String.format("%s%s%s", "Set-Cookie:", entry.toString(), CRLF));
        }

        sb.append(CRLF);
        
        ByteArrayMessage msg = new ByteArrayMessage();
        msg.array = sb.toString().getBytes(this.charset());
        msg.offset = 0;
        msg.length = msg.array.length;
        msg.handler = channel.handler;
        channel.enqueue(msg);
        headerSent = true;
    }

    private synchronized void transferFile(String filename, long position, long length) throws IOException {
        this.flush();

        if (endFlush) {
            throw new IOException("错误的传输方式");
        } else if (chunked) {
            contentLength += length;
            ByteArrayMessage msg = new ByteArrayMessage();
            msg.array = String.format("%s %s", Long.toHexString(length), CRLF).getBytes(this.charset());
            msg.offset = 0;
            msg.length = msg.array.length;
            msg.handler = channel.handler;
            channel.enqueue(msg);

            FileReginMessage fmsg = new FileReginMessage();
            fmsg.handler = channel.handler;
            fmsg.filename = filename;
            fmsg.position = position;
            fmsg.length = length;
            channel.enqueue(fmsg);

            msg = new ByteArrayMessage();
            msg.array = String.format("%s", CRLF).getBytes(this.charset());
            msg.offset = 0;
            msg.length = msg.array.length;
            msg.handler = channel.handler;
            channel.enqueue(msg);

        } else {
            contentLength -= length;
            FileReginMessage fmsg = new FileReginMessage();
            fmsg.handler = channel.handler;
            fmsg.filename = filename;
            fmsg.position = position;
            fmsg.length = length;
            channel.enqueue(fmsg);
        }

    }

    @Override
    public HttpHeaderCollection headers() {
        return headers;
    }

    @Override
    public void setHeader(String name, String text) throws InvalidOperationException {
        checkAndThrowHeaderSent();
        if (headers.containsKey(name)) {
            headers.get(name).text(text);
        } else {
            headers.put(new HttpHeader(name, text));
        }
    }

    @Override
    public void setHeader(HttpHeader header) throws InvalidOperationException {
        checkAndThrowHeaderSent();
        if (headers.containsKey(header.name())) {
            this.setHeader(header.name(), header.text());
        } else {
            headers.put(header);
        }
    }

    @Override
    public void setContentLength(long length) throws InvalidOperationException {
        checkAndThrowHeaderSent();
        auto = false;
        contentLength = length;
        chunked = false;
    }

    @Override
    public boolean headerSent() {
        return headerSent;
    }

    @Override
    public boolean isConnected() {
        return channel.isOpen();
    }

    @Override
    public void write(byte[] array, int offset, int length) {

        if (!this.buffering()) {
            buffer = ByteBuffer.wrap(array, offset, length);
            this.flush();

        } else {
            if (buffer != null) {
                int remaining = buffer.remaining() - length;
                if (remaining >= 0) {
                    buffer.put(array, offset, length);
                } else {
                    buffer.put(array, offset, buffer.remaining());
                    flush();
                    this.write(array, offset + length + remaining + length, Math.abs(remaining + length));
                }

            } else {
                contentLength += length;
                buffer=ByteBuffer.allocate(1024>length?1024:length);
                buffer.put(array, offset, length);
            }
        }

    }

    @Override
    public void transmit(String filename) throws FileNotFoundException, IOException {
        try (FileChannel chan = new FileInputStream(filename).getChannel()) {
            this.transferFile(filename, 0, chan.size());
        }
    }

    @Override
    public void transmit(String filename, long position, long length) throws FileNotFoundException, IOException {
        this.transferFile(filename, position, length);
    }

    @Override
    public void flush() {
        if (!headerSent) {
            if (!endFlush && auto) {
                chunked = true;
            }
            writeHeaders();
        }

        if (buffer != null && this.buffering()) {
            buffer.flip();
        }

        if (chunked) {
            ByteArrayMessage msg;
            if (buffer != null) {
                contentLength += buffer.limit();
                msg = new ByteArrayMessage();
                msg.array = String.format("%s %s", Long.toHexString(buffer.limit()), CRLF).getBytes(this.charset());
                msg.offset = 0;
                msg.length = msg.array.length;
                msg.handler = channel.handler;
                channel.enqueue(msg);

                ByteBufferMessage msg2 = new ByteBufferMessage();
                msg2.buffer = buffer;
                msg2.handler = channel.handler;
                channel.enqueue(msg2);

                msg = new ByteArrayMessage();
                msg.array = String.format("%s", CRLF).getBytes(this.charset());
                msg.offset = 0;
                msg.length = msg.array.length;
                msg.handler = channel.handler;
                channel.enqueue(msg);
            }

            if (endFlush) {
                msg = new ByteArrayMessage();
                msg.array = String.format("0%s%s", CRLF, CRLF).getBytes(this.charset());
                msg.offset = 0;
                msg.length = msg.array.length;
                msg.handler = channel.handler;
                channel.enqueue(msg);
            }

        } else if (buffer != null) {
            contentLength -= buffer.limit();
            ByteBufferMessage msg2 = new ByteBufferMessage();
            msg2.buffer = buffer;
            msg2.handler = channel.handler;
            channel.enqueue(msg2);
        }

        buffer = null;
    }

    @Override
    public void end() {
        endFlush = true;
        flush();
        channel.enqueue(new Message() {

            @Override
            public void process(Channel channel, ChannelHanlder handler) throws IOException {
                channel.close();
            }

            @Override
            public ChannelHanlder getHandler() {
                return channel.handler;
            }

            @Override
            public void reset() {
            }
        });
    }

}
