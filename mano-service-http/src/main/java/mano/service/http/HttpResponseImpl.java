/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.service.http;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mano.ArgumentNullException;
import mano.DateTime;
import mano.InvalidOperationException;
import mano.io.ChannelContext;
import mano.io.ChannelException;
import mano.io.ChannelFuture;
import mano.io.FileRegion;
import mano.net.http.HttpCookieCollection;
import mano.net.http.HttpHeader;
import mano.net.http.HttpHeaderCollection;
import mano.net.http.HttpResponse;
import mano.service.http.HttpListener.HttpContextImpl;

/**
 *
 * @author sixmoon
 */
class HttpResponseImpl extends HttpResponse {

    HttpHeaderCollection headers = new HttpHeaderCollection();
    private boolean headerSent;
    private long contentLength;
    private boolean explicitContentLength;
    public boolean done;
    private boolean end;
    private boolean chunked;
    private boolean endFlush;
    private static final String CRLF = "\r\n";
    public boolean keepAlive;
    public HttpContextImpl context;

    @Override
    public HttpHeaderCollection headers() {
        return headers;
    }

    @Override
    public void setHeader(HttpHeader header) {
        if (header == null) {
            throw new ArgumentNullException("header");
        }
        checkAndThrowHeaderSent();
        headers.put(header);
    }

    @Override
    public void setContentLength(long length) throws InvalidOperationException {
        checkAndThrowHeaderSent();
        contentLength = length;
        explicitContentLength = true;
    }

    @Override
    public boolean headerSent() {
        return headerSent;
    }

    @Override
    public boolean isConnected() {
        return context.isOpen();
    }

    ByteBuffer contentBuffer;
    List<ByteBuffer> buffers = new ArrayList<>();

    @Override
    public void write(byte[] buffer, int offset, int count) {

        if (contentBuffer != null) {
            int len = Math.min(count, contentBuffer.remaining());
            contentBuffer.put(buffer, offset, len);
            if (!explicitContentLength) {
                this.contentLength += len;
            }
            if (!contentBuffer.hasRemaining()) {
                contentBuffer.flip();
                buffers.add(contentBuffer);
                contentBuffer = null;
            }
            if (len - count < 0) {
                //TODO:contentBuffer=pool.get();
                write(buffer, offset + len, count - len);
                return;
            }
        } else {
            if (!explicitContentLength) {
                this.contentLength += count;
            }
            buffers.add(ByteBuffer.wrap(buffer, offset, count));
        }

        if (!this.buffering()) {
            this.flush();
        }
    }

    @Override
    public void transmit(String filename) throws FileNotFoundException, IOException {
        File file = new File(filename);
        if (file.exists() || file.isFile()) {
            this.transferFile(filename, 0, file.length());
        } else {
            throw new FileNotFoundException(filename);
        }
    }

    @Override
    public void transmit(String filename, long position, long length) throws FileNotFoundException, IOException {
        File file = new File(filename);
        if (file.exists() || file.isFile()) {
            this.transferFile(filename, position, length);
        } else {
            throw new FileNotFoundException(filename);
        }
    }

    private synchronized void transferFile(String filename, long position, long length) throws IOException {
        this.flush();

        if (endFlush) {
            throw new IOException("错误的传输方式");
        } else if (chunked) {
            send(String.format("%s %s", Long.toHexString(length), CRLF).getBytes(this.charset()));
            context.channel().write(filename, position, length);
            send(String.format("%s", CRLF).getBytes(this.charset()));
        } else {
            //contentLength -= length;
            context.channel().write(filename, position, length);
        }
    }

    @Override
    public void flush() {
        if (!headerSent) {

            if (!explicitContentLength && !endFlush) {
                chunked = true;
            }

            if (!headers.containsKey("Date")) {
                this.setHeader("Date", DateTime.now().toGMTString());
            }

            if (!headers.containsKey("Connection")) {
                this.setHeader("Connection", keepAlive ? "keep-alive" : "close");
            }

            if (!headers.containsKey("Content-Type")) {
                this.setHeader("Content-Type", "text/html;charset=utf-8");
            }

            if (!this.chunked || this.endFlush || explicitContentLength) {
                this.setHeader("Content-Length", this.contentLength + "");
            } else {
                this.setHeader("Transfer-Encoding", "chunked");
            }
            headerSent = true;
            StringBuilder sb = new StringBuilder();
            sb.append("HTTP/1.1").append(" ").append(this.status()).append(" ").append(this.statusDescription()).append(CRLF);
            for (HttpHeader header : headers.values()) {
                sb.append(header.name()).append(":").append(header.text()).append(CRLF);
            }
            for (HttpCookieCollection.CookieEntry entry : this.getCookie().iterator()) {
                sb.append("Set-Cookie:").append(entry.toString()).append(CRLF);
            }
            sb.append(CRLF);

            send(sb.toString().getBytes(this.charset()));

        }

        if (chunked) {
            /*if(contentLength==0 && endFlush){
             send(String.format("0%s%s", CRLF, CRLF).getBytes(this.charset()));
             }else */
            if (contentLength > 0) {
                send(String.format("%s %s", Long.toHexString(contentLength), CRLF).getBytes(this.charset()));
                for (ByteBuffer buf : buffers) {
                    send(buf);
                }
                buffers.clear();
                contentLength = 0;
                send(String.format("%s", CRLF).getBytes(this.charset()));
            }

            if (endFlush) {
                send(String.format("0%s%s", CRLF, CRLF).getBytes(this.charset()));
            }

        } else {
            for (ByteBuffer buf : buffers) {
                send(buf);
            }
            buffers.clear();
        }

        if (endFlush) {
            try {
                context.channel().submit(new CloseChannelFuture().fresh());
            } catch (ChannelException ex) {
                context.handleError(ex);
            } finally {
                this.done = true;
            }
        }
    }

    class CloseChannelFuture extends ChannelFuture {

        public CloseChannelFuture() {
            super(null, ChannelFuture.OP_WRITE);
        }

        @Override
        protected void doExecute(ChannelContext context) {
            if (keepAlive) {
                context.channel().close();
                //TODO:
            } else {
                context.channel().close();
            }
        }
    }

    void send(byte[] buf) {
        try {
            context.channel().write(buf);
        } catch (ChannelException ex) {
            context.handleError(ex);
        }
    }

    void send(ByteBuffer buf) {
        try {
            context.channel().write(buf, false);
        } catch (ChannelException ex) {
            context.handleError(ex);
        }
    }

    @Override
    public void end() {
        if (done) {
            return;
        }
        this.endFlush = true;
        this.flush();
    }

}
