/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.web.runtime;

import com.diosay.mano.io.ByteArrayMessage;
import com.diosay.mano.io.ChannelBuffer;
import com.diosay.mano.io.ChannelCloseingMessage;
import com.diosay.mano.io.ChannelHanlder;
import java.io.PrintWriter;
import java.io.StringWriter;
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
        //channel.handler = this;
        channel.buffer = channel.getListener().getGroup().allocate();
        channel.start();
        channel.read(channel.buffer);
    }

    @Override
    public void closed(HttpChannel channel) {
        channel.getListener().getGroup().free(channel.buffer);
        channel.buffer=null;
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
                    channel.request.channel = channel;
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
                channel.read(buffer);
            }
        } else if (channel.phase == HttpChannel.RESPONSE) {
            if (channel.request == null || channel.request.decoder == null) {
                throw new IllegalStateException("未设置处理程序");
            }

            int pos = buffer.position();
            channel.request.decoder.onRead(buffer, channel.request);
            channel.request.remaining -= buffer.position() - pos;

            if (channel.request.loadedFlag.get() && (channel.request.remaining > 0 || buffer.hasRemaining())) {
                channel.request.remaining -= buffer.remaining();
                buffer.position(buffer.limit());//TODO
            }

            if (channel.request.remaining > 0) {
                if (!buffer.hasRemaining()) {
                    buffer.clear();
                } else {
                    buffer.compact();
                }
                channel.read(buffer);
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
        }else if(exc.getMessage()!=null && exc.getMessage().indexOf("connection was aborted")>0){
            channel.close();
            return;
        }else if(!channel.isOpen()){
            channel.close();
            return;
        }
        System.err.println("ERR:");
        exc.printStackTrace(System.err);
        this.responseError(exc, channel);
    }

    private void printRoot(StringBuilder sb, Throwable t) {
        if (t == null) {
            return;
        }
        sb.append("<b>root</b><p><pre>");
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            pw.println();
            t.printStackTrace(pw);
        }
        sb.append(sw.toString());
        sb.append("</pre></p>");
        printRoot(sb, t.getCause());
    }

    public void responseError(Throwable t, HttpChannel channel) {
        HttpStatus status;
        StringBuilder sb = new StringBuilder();
        if (t instanceof HttpException) {
            HttpException ex = (HttpException) t;
            status = ex.getHttpStatus();
            sb.append("<html><head><title>")
                    .append(ex.getHttpStatus().getStatus())
                    .append(" Error")
                    .append("</title></head><body>");
            if (ex.getMessage() != null) {
                sb.append("<b>message</b><u>")
                        .append(ex.getMessage())
                        .append("</u>");
            }
            if (ex.getCause() != null) {
                sb.append("<b>exception</b><p><pre>");
                StringWriter sw = new StringWriter();
                try (PrintWriter pw = new PrintWriter(sw)) {
                    pw.println();
                    ex.getCause().printStackTrace(pw);
                }
                sb.append(sw.toString());
                sb.append("</pre></p>");
                printRoot(sb, ex.getCause().getCause());
            }
            sb.append("<hr>")
                    .append("Mano Server");//context.getServer().getVersion()
        } else {
            status = HttpStatus.InternalServerError;
            sb.append("<html><head><title>")
                    .append(HttpStatus.InternalServerError.getStatus())
                    .append(" Error")
                    .append("</title></head><body>");
            if (t.getMessage() != null) {
                sb.append("<b>message</b><u>")
                        .append(t.getMessage())
                        .append("</u>");
            }
            if (t.getCause() != null) {
                sb.append("<b>exception</b><p><pre>");
                StringWriter sw = new StringWriter();
                try (PrintWriter pw = new PrintWriter(sw)) {
                    pw.println();
                    t.getCause().printStackTrace(pw);
                }
                sb.append(sw.toString());
                sb.append("</pre></p>");
                printRoot(sb, t.getCause().getCause());
            }
            sb.append("<hr>")
                    .append("Mano Server");//context.getServer().getVersion()
        }
        sb.append("</body></html>");
        byte[] response = sb.toString().getBytes(channel.getInputEncoding());
        sb = new StringBuilder("HTTP/1.1 ");
        sb.append(status.getStatus()).append(" ").append(status.getDescription()).append("\r\n");
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

//    public void responseError(HttpException ex, HttpChannel channel) {
//        StringWriter sw = new StringWriter();
//        try (PrintWriter pw = new PrintWriter(sw)) {
//            pw.println();
//            ex.printStackTrace(pw);
//        }
//        byte[] response = String.format("<html><head><title>HTTP %s Error</title></head><body>%s</body></html>", ex.getHttpCode(), sw.toString()).getBytes();
//        StringBuilder sb = new StringBuilder("HTTP/1.1 ");
//        sb.append(ex.getHttpCode()).append(" ").append(HttpStatus.getKnowDescription(ex.getHttpCode())).append("\r\n");
//        sb.append("Content-Length:").append(response.length).append("\r\n");
//        sb.append("Connection:close").append("\r\n");
//        sb.append("Date:").append(DateTime.now().toGMTString()).append("\r\n");
//        sb.append("\r\n");
//
//        byte[] bytes = sb.toString().getBytes(channel.getInputEncoding());
//        ByteArrayMessage msg = new ByteArrayMessage();
//        msg.handler = this;
//        msg.array = bytes;
//        msg.offset = 0;
//        msg.length = bytes.length;
//        channel.enqueue(msg);
//
//        msg = new ByteArrayMessage();
//        msg.handler = this;
//        msg.array = response;
//        msg.offset = 0;
//        msg.length = response.length;
//        channel.enqueue(msg);
//
//        ChannelCloseingMessage msg2 = new ChannelCloseingMessage();
//        msg2.handler = this;
//        channel.enqueue(msg2);
//
//    }
}
