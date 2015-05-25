/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.net.http;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import mano.InvalidOperationException;
import mano.io.BufferUtil;

/**
 * 多部分实体解码器
 *
 * @author jun <jun@diosay.com>
 */
public class HttpMultipartDecoder implements HttpEntityBodyDecoder {

    private class ByteBufferBuffer {

        public final ByteBuffer buffer;

        public ByteBufferBuffer(byte[] array, int index, int length) {
            buffer = ByteBuffer.wrap(array, index, length);
        }

        public ByteBufferBuffer(byte[] array) {
            buffer = ByteBuffer.wrap(array, 0, array.length);
        }

        public ByteBufferBuffer(int capacity) {
            buffer = ByteBuffer.allocate(capacity);
        }

        public ByteBufferBuffer(ByteBuffer inner) {
            buffer = inner;
        }

        public byte[] array() {
            return buffer.array();
        }

        public int offset() {
            return buffer.arrayOffset();
        }

        /**
         * 获取缓冲区的容量。
         *
         * @return
         */
        public int capacity() {
            return buffer.capacity();
        }

        /**
         * 当前缓冲的位置。
         *
         * @return
         */
        public int position() {
            return buffer.position();
        }

        /**
         * 设置新的缓冲区偏移。
         *
         * @param pos
         */
        public void position(int pos) {
            buffer.position(pos);
        }

        /**
         * 获取有效数据大小。
         *
         * @return
         */
        public int limit() {
            return buffer.limit();
        }

        /**
         * 获取剩余数量
         *
         * @return
         */
        public int remaining() {
            return buffer.remaining();
        }

        /**
         * 缓冲区是否已满。
         *
         * @return
         */
        public boolean isFull() {
            return this.position() == 0 && !this.hasRemaining();
        }

        /**
         * 缓冲区是否还有数据。
         *
         * @return
         */
        public boolean hasRemaining() {
            return buffer.hasRemaining();
        }

        /**
         * 清空缓冲区。
         */
        public void clear() {
            buffer.clear();
        }

        /**
         * 将缓冲区数据对齐到开始位置。
         */
        public void compact() {
            buffer.compact();
        }

        public synchronized String readln(String charset) throws UnsupportedEncodingException {
            return readln(Charset.forName(charset));
        }

        public synchronized String readln(Charset charset) {
            int off = buffer.arrayOffset() + buffer.position();
            if (buffer.limit() < BufferUtil.CRLF.length) {
                return null;
            }
            int index = BufferUtil.bytesIndexOf(array(), off, buffer.remaining(), BufferUtil.CRLF);
            if (index < 0) {
                return null;
            }
            String result = readstr(off, index - off, charset);
            buffer.position(buffer.position() + BufferUtil.CRLF.length);
            return result;
        }

        public String readln() throws UnsupportedEncodingException {
            return readln("UTF-8");
        }

        public String readstr(int off, int count, String charset) throws UnsupportedEncodingException {
            return this.readstr(off, count, Charset.forName(charset));
        }

        public synchronized String readstr(int off, int count, Charset charset) {
            String result = new String(array(), off, count, charset);
            buffer.position(buffer.position() + count);
            return result;
        }

        public String readstr(int off, int count) throws UnsupportedEncodingException {
            return readstr(off, count, "UTF-8");
        }

        public synchronized String readstr(String charset) throws UnsupportedEncodingException {
            return readstr(offset() + buffer.position(), buffer.remaining(), charset);
        }

        public String readstr() throws UnsupportedEncodingException {
            return readstr("UTF-8");
        }

    }

    final int IDLE = 0, TOKEN = 1, HEADER = 2, DATA = 3, FILE = 4, FORM = 5;//HttpRequestAppender Channel
    int state = 0;
    byte[] _endBoundary;
    byte[] _boundary;
    boolean eof = false;
    int index = -1;
    int type = 0;
    long size;
    HttpHeaderCollection headers = new HttpHeaderCollection();
    OutputStream out;
    File file;
    boolean done;

    private <T extends HttpEntityBodyAppender> void onRead(ByteBufferBuffer buffer, T appender) throws Exception {
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

    private int findBoundary(boolean first, ByteBufferBuffer buffer) {

        if (first) {
            index = BufferUtil.bytesIndexOf(buffer.array(), buffer.offset() + buffer.position(), buffer.remaining(), _boundary, BufferUtil.CRLF.length, _boundary.length - BufferUtil.CRLF.length);
        } else {
            index = BufferUtil.bytesIndexOf(buffer.array(), buffer.offset() + buffer.position(), buffer.remaining(), _boundary);
            if (index < 0) {
                index = BufferUtil.bytesIndexOf(buffer.array(), buffer.offset() + buffer.position(), buffer.remaining(), _endBoundary);
                if (index > -1) {
                    eof = true;
                }
            }
        }
        return index;
    }

    <T extends HttpEntityBodyAppender> void readHeaders(ByteBufferBuffer buffer, T appender) throws Exception {
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
                    file = File.createTempFile("post_", ".tmp");//TODO:, new File("D:\\tmp")
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

    <T extends HttpEntityBodyAppender> void data(ByteBufferBuffer buffer, T appender) throws Exception {
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

    <T extends HttpEntityBodyAppender> void done(ByteBufferBuffer buffer, T appender) throws Exception {
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
            done = true;
            appender.notifyDone();
        } else {
            onRead(buffer, appender);
        }
    }

    @Override
    public <T extends HttpEntityBodyAppender> void decode(T appender) throws IOException {
        //System.out.println("content len:"+appender.getContentLength());
        byte[] array = new byte[1024 * 4];
        ByteBufferBuffer buffer = new ByteBufferBuffer(ByteBuffer.allocate(1024*8));
        long tot=0;
        try (InputStream stream = appender.getEntityBodyStream()) {
            int reads;
            buffer.clear();
            do {
                reads = stream.read(array, 0, Math.min(buffer.buffer.remaining(), array.length));
                //System.out.println("reads:"+reads);
                if (reads < 1) {
                    break;
                }
                tot+=reads;
                buffer.buffer.put(array, 0, reads);
                buffer.buffer.flip();
                //buffer.buffer.limit(buffer.position() + reads);
                try {
                    this.onRead(buffer, appender);
                } catch (Exception ex) {
                    throw new IOException(ex);
                }
                buffer.buffer.compact();
            } while (true);
            //System.out.println("read len:"+tot+" rm:"+buffer.buffer.remaining()+"/done:"+done);
//            while ((limit = stream.read(array)) > 0) {
//                buffer.clear();
//                buffer.buffer.limit(limit);
//                try {
//                    this.onRead(buffer, appender);
//                } catch (Exception ex) {
//                    throw new IOException(ex);
//                }
//            }
        }
    }

}
