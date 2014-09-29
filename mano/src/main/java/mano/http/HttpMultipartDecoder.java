/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

import com.diosay.mano.io.ChannelBuffer;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import mano.InvalidOperationException;

/**
 * 多部分实体解码器
 *
 * @author jun <jun@diosay.com>
 */
public class HttpMultipartDecoder implements HttpEntityBodyDecoder {

    final int IDLE = 0, TOKEN = 1, HEADER = 2, DATA = 3, FILE = 4, FORM = 5;//HttpRequestAppender Channel
    int state = 0;
    byte[] _endBoundary;
    byte[] _boundary;
    byte[] CRLF = "\r\n".getBytes();
    boolean eof = false;
    int index = -1;
    int type = 0;
    long size;
    HttpHeaderCollection headers = new HttpHeaderCollection();
    OutputStream out;
    File file;
    boolean done;
    @Override
    public <T extends HttpEntityBodyAppender> void onRead(ChannelBuffer buffer, T appender) throws Exception {
        if (done) {
            buffer.position(buffer.limit());//TODO
            return;
        }
        if (state == IDLE) {
            _endBoundary = ("\r\n" + appender.getBoundary() + "--").getBytes();
            _boundary = ("\r\n" + appender.getBoundary() + "\r\n").getBytes();
            state = TOKEN;
            onRead(buffer, appender);
        } else if (state == TOKEN) {
            index = findBoundary(true, buffer);
            if (index < 0) {
                if (buffer.isFull()) {//full
                    throw new InvalidOperationException("Miss multipart Boundary.");
                }
            } else {
                buffer.position(buffer.position() + (index - (buffer.offset() + buffer.position())) + (_boundary.length - 2));//吃掉分界符
                state = HEADER;
                onRead(buffer, appender);
            }
        } else if (state == HEADER) {
            this.readHeaders(buffer, appender);
        } else if (state == DATA) {
            this.data(buffer, appender);
        }

    }

    private int findBoundary(boolean first, ChannelBuffer buffer) {

        if (first) {
            index = ChannelBuffer.bytesIndexOf(buffer.array(), buffer.offset() + buffer.position(), buffer.remaining(), _boundary, CRLF.length, _boundary.length - CRLF.length);
        } else {
            index = ChannelBuffer.bytesIndexOf(buffer.array(), buffer.offset() + buffer.position(), buffer.remaining(), _boundary);
            if (index < 0) {
                index = ChannelBuffer.bytesIndexOf(buffer.array(), buffer.offset() + buffer.position(), buffer.remaining(), _endBoundary);
                if (index > -1) {
                    eof = true;
                }
            }
        }
        return index;
    }

    <T extends HttpEntityBodyAppender> void readHeaders(ChannelBuffer buffer, T appender) throws Exception {
        String line;
        while ((line = buffer.readln(appender.getEncoding())) != null) {
            if ("".equals(line)) {
                HttpHeader header = headers.get("Content-Disposition");
                if (header == null) {
                    throw new HttpException(HttpStatus.BadRequest, "Miss the Multipart-Entity Header Content-Disposition.");
                }
                String fn = header.attr("filename");
                if (fn == null || "".equals(fn.trim())) {
                    type = FORM;
                    out = new ByteArrayOutputStream();
                } else {
                    type = FILE;
                    file = File.createTempFile("post_", ".tmp");
                    out = new FileOutputStream(file);
                }
                state = DATA;
                onRead(buffer, appender);
                return;
            } else {
                HttpHeader header = HttpHeader.prase(line);
                if (header == null) {
                    throw new HttpException(HttpStatus.BadRequest, "Multipart Entity Header can not be resolved.");
                }
                state = HEADER;
                headers.put(header);
            }
        }
        if (buffer.isFull()) {//full
            throw new InvalidOperationException("multipart Header too large.");
        }
    }

    <T extends HttpEntityBodyAppender> void data(ChannelBuffer buffer, T appender) throws Exception {
        index = findBoundary(false, buffer);
        int count = 0;
        int off = buffer.offset() + buffer.position();
        if (index < 0) {
            //保存已经读取的数据
            count = buffer.remaining();

            if (count >= _boundary.length) {
                count -= (_boundary.length);
            } else if (!buffer.hasRemaining()) {
                count = 1;
            }

            if (count > 0) {
                out.write(buffer.array(), off, count);
                buffer.position(buffer.position() + count);
                size += count;
            }

            if (buffer.isFull()) {//full
                throw new InvalidOperationException("buffer has been full.");
            }

        } else {
            count = index - off;
            if (count > 0) {
                out.write(buffer.array(), off, count);
                size += count;
            }
            buffer.position(buffer.position() + count + (eof ? _endBoundary.length : _boundary.length));
            state = HEADER;
            this.done(buffer, appender);
        }
    }

    <T extends HttpEntityBodyAppender> void done(ChannelBuffer buffer, T appender) throws Exception {
        HttpHeader header = headers.get("Content-Disposition");

        if (type == FILE) {
            HttpPostFile pf = new HttpPostFile(file,
                    header.attr("name"),
                    header.attr("filename"),
                    headers.containsKey("Content-Type") ? headers.get("Content-Type").value() : "",
                    size);
            out.flush();
            out.close();
            appender.appendPostFile(pf);
        } else {
            appender.appendFormItem(header.attr("name"), ((ByteArrayOutputStream) out).toString(appender.getEncoding().name()));
            out.close();
        }
        out = null;
        headers.clear();
        file = null;
        if (eof) {
            state = IDLE;
            eof = false;
            done=true;
            appender.notifyDone();
        } else {
            onRead(buffer, appender);
        }
    }

}
