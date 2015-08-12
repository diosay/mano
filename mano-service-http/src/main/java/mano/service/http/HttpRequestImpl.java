/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.service.http;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import mano.InvalidOperationException;
import mano.io.TempOutputStream;
import mano.logging.Log;
import mano.net.http.HttpEntityBodyAppender;
import mano.net.http.HttpFormUrlEncodedDecoder;
import mano.net.http.HttpHeaderCollection;
import mano.net.http.HttpMethod;
import mano.net.http.HttpMultipartDecoder;
import mano.net.http.HttpPostFile;
import mano.net.http.HttpRequest;
import mano.net.http.HttpVersion;
import mano.service.http.HttpListener.HttpContextImpl;
import mano.util.NameValueCollection;

/**
 *
 * @author sixmoon
 */
class HttpRequestImpl extends HttpRequest implements HttpEntityBodyAppender {

    HttpMethod method;
    String rawUrl;
    HttpVersion version;
    HttpHeaderCollection headers = new HttpHeaderCollection();
    HttpContextImpl context;

    private final AtomicBoolean decodeFlag = new AtomicBoolean(false);
    private final AtomicBoolean loadedFlag = new AtomicBoolean(false);
    private Map<String, String> form;
    private Map<String, HttpPostFile> files;
    private long contentLength;
    private long remaining;
    private boolean hasEntityBody;
    private boolean isChunked;
    private boolean isFormUrlEncoded;
    private boolean isFormMultipart;
    private String boundary;
    //HttpEntityBodyDecoder decoder;
    private boolean pred;
    private TempOutputStream out;
    private final ReentrantLock lock = new ReentrantLock();
    final AtomicReference<ByteBuffer> loadBuffer = new AtomicReference<>();
    java.util.concurrent.Semaphore readLock = new java.util.concurrent.Semaphore(1);

    private synchronized void pre() {
        if (pred) {
            return;
        }
        pred = true;
        form = new NameValueCollection<>();
        files = new NameValueCollection<>();
        if (headers.containsKey("Content-length")) {
            try {
                remaining = contentLength = Long.parseLong(headers.get("Content-Length").value());
            } catch (Throwable t) {

            }
        }

        if ((HttpMethod.POST.equals(method) || HttpMethod.PUT.equals(method))) {

            if (headers.containsKey("Transfer-Encoding")
                    && "chunked".equalsIgnoreCase(headers.get("Transfer-Encoding").value())) {
                isChunked = true;
                hasEntityBody = true;
            }

            if (headers.containsKey("Content-Type")) {
                if ("application/x-www-form-urlencoded".equalsIgnoreCase(headers.get("Content-Type").value())) {
                    hasEntityBody = true;
                    isFormUrlEncoded = true;
                } else if ("multipart/form-data".equalsIgnoreCase(headers.get("Content-Type").value())) {
                    isFormMultipart = true;
                    boundary = "--" + headers.get("Content-Type").attr("boundary");
                    hasEntityBody = true;
                }
            }
        }

        if (!isChunked && this.remaining <= 0) {
            loadedFlag.set(true);
        }
    }

    void writeEntityBody(ByteBuffer buffer) {
        if (buffer == null) {
            throw new java.lang.NullPointerException("buffer");
        }
        //System.out.println("in 1");
//        synchronized (loadBuffer) {
//            loadBuffer.set(buffer);
//            loadBuffer.notifyAll();
//        }

        loadBuffer.set(buffer);
        readLock.release();
//        try{
//        lock.unlock();
//        }catch(Throwable t){}
//        lock.lock();
//        try {
//            loadBuffer.set(buffer);
//        } finally {
//            lock.unlock();
//        }
    }

    private void decodeEntityBody() {
        loadEntityBody();
        if (decodeFlag.get() || !hasEntityBody) {
            return;
        }
        if (isChunked) {
            throw new UnsupportedOperationException("Not supported chunked encoding.");
        } else if (isFormUrlEncoded) {
            try {
                decodeFlag.set(true);
                new HttpFormUrlEncodedDecoder().decode(this);
            } catch (Exception ex) {
                throw new InvalidOperationException(ex);
            }
        } else if (isFormMultipart) {
            try {
                decodeFlag.set(true);
                //System.out.println("here");
                new HttpMultipartDecoder().decode(this);

//                synchronized (decodeFlag) {
//                    this.decodeFlag.set(true);
//                    this.decodeFlag.notifyAll();
//                }
            } catch (Exception ex) {
                throw new InvalidOperationException(ex);
            }
        } else {
            if (Log.TRACE.isDebugEnabled()) {
                Log.TRACE.debug("未找到标准实体内容解码器，请自行解码。");
            }
            //throw new UnsupportedOperationException("Not supported decoding."+headers.get("Content-Type").text());
        }
    }

    @Override
    public HttpVersion getVersion() {
        return version;
    }

    @Override
    public HttpMethod getMethod() {
        return method;
    }

    @Override
    public HttpHeaderCollection headers() {
        return headers;
    }

    @Override
    public String rawUrl() {
        return rawUrl;
    }

    @Override
    public long getContentLength() {
        loadEntityBody();
        return contentLength;
    }

    @Override
    public Map<String, String> form() {
        decodeEntityBody();
        return form;
    }

    @Override
    public Map<String, HttpPostFile> files() {
        decodeEntityBody();
        return files;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public boolean isConnected() {
        return context.isOpen();
    }

    @Override
    public boolean canLoadEntityBody() {
        pre();
        return hasEntityBody && !loadedFlag.get();
    }

    @Override
    public void loadEntityBody() throws InvalidOperationException {
        if (!canLoadEntityBody()) {
            return;
        }

        if (out == null) {
            out = new TempOutputStream();
        }
        loadBuffer.set(context.keepBuffer);
        //writeEntityBody(context.keepBuffer);
        ByteBuffer buffer = null;
        if (!loadedFlag.get() && loadBuffer.get() == null) {
            try {
                //lock.lock();
                readLock.acquire();
                this.context.channel().read();
            } catch (Exception ex) {
                throw new InvalidOperationException(ex);
            }
        }

        while (!loadedFlag.get()) {//
//            if (loadBuffer.get() == null) {
//                synchronized (loadBuffer) {
//                    try {
//                        Thread.yield();
//                        loadBuffer.wait(1000 * 60);//TODO:等待超时？ 大文件？
//                    } catch (InterruptedException ex) {
//                        this.context.channel().close();
//                        throw new InvalidOperationException(ex);
//                    }
//                }
//            }

            try {
                //System.out.println("in2");
                if (readLock.tryAcquire(5, TimeUnit.SECONDS)) {
                    if (loadBuffer.get() == null) {
                        Thread.yield();
                        continue;
                    } else {
                        //lock.unlock();
                        readLock.release();
                        buffer = loadBuffer.getAndSet(null);
                    }
                } else {
                    this.context.channel().close();
                    throw new InvalidOperationException("等待数据上传超时。");
                }
            } catch (InterruptedException ex) {
                this.context.channel().close();
                throw new InvalidOperationException(ex);
            }

            //System.out.println("in3");
            if (buffer == null) {
                throw new InvalidOperationException("等待数据上传超时。");
            }
            this.remaining -= buffer.remaining();

            try {
                out.write(buffer);
            } catch (IOException ex) {
                ex.printStackTrace();
                throw new InvalidOperationException(ex);
            }

            //System.out.println("body reading :" + remaining + "/" + contentLength);
            if (this.remaining > 0) {
                buffer.clear();
                try {
                    //lock.lock();
                    readLock.acquire();
                    this.context.channel().read(buffer);
                } catch (Exception ex) {
                    throw new InvalidOperationException(ex);
                }
            } else {
                synchronized (loadedFlag) {
                    loadedFlag.set(true);
                    loadedFlag.notifyAll();
                    break;
                }
            }
        }

        //System.out.println("status:" + loadedFlag.get() + " buf: " + loadBuffer.get());
//        synchronized (loadedFlag) {
//            if (!loadedFlag.get()) {
//                try {
//                    loadedFlag.wait(1000 * 60 * 5);//TODO:等待超时？ 大文件？
//                } catch (InterruptedException ex) {
//                    this.context.channel().close();
//                    throw new InvalidOperationException(ex);
//                }
//                if (!loadedFlag.get()) {
//                    throw new InvalidOperationException("等待数据上传超时。");
//                }
//            }
//        }
    }

    @Override
    public InputStream getEntityBodyStream() throws IOException {
        if (out == null) {
            return null;
        }
        return out.toInputStream();
    }

    @Override
    public void Abort() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getBoundary() {
        return this.boundary;
    }

    @Override
    public void appendPostFile(HttpPostFile file) {
        files.put(file.getName(), file);
    }

    @Override
    public void appendFormItem(String name, String value) {
        value = value == null ? "" : value;
        if (form.containsKey(name)) {
            String old = form.get(name);
            value = (old == null ? "" : old)+","+value;
        }
        form.put(name, value);
    }

    @Override
    public void notifyDone() {
        synchronized (decodeFlag) {
            this.decodeFlag.set(true);
            this.decodeFlag.notifyAll();
        }
    }

    @Override
    public Charset getEncoding() {
        return mano.io.CharsetUtil.UTF8;
    }

}
