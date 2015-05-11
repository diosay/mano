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
import java.util.concurrent.atomic.AtomicBoolean;
import mano.InvalidOperationException;
import mano.io.ChannelException;
import mano.io.TempOutputStream;
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
    
    private synchronized void pre() {
        if (pred) {
            return;
        }
        pred = true;
        form = new NameValueCollection<>();
        files = new NameValueCollection<>();
        if (headers.containsKey("Content-length")) {
            remaining = contentLength = Long.parseLong(headers.get("Content-Length").value());
        }

        if (contentLength >= 0 && (HttpMethod.POST.equals(method) || HttpMethod.PUT.equals(method))) {
            hasEntityBody = true;
            if (headers.containsKey("Transfer-Encoding")
                    && "chunked".equalsIgnoreCase(headers.get("Transfer-Encoding").value())) {
                isChunked = true;
            }

            if (headers.containsKey("Content-Type")) {

                if ("application/x-www-form-urlencoded".equalsIgnoreCase(headers.get("Content-Type").value())) {
                    isFormUrlEncoded = true;
                } else if ("multipart/form-data".equalsIgnoreCase(headers.get("Content-Type").value())) {
                    isFormMultipart = true;
                    boundary = "--" + headers.get("Content-Type").attr("boundary");
                }
            }
        }
    }

    void writeEntityBody(ByteBuffer buffer) {
        if (buffer == null) {
            return;
        }
        this.remaining -= buffer.remaining();
        while (buffer.hasRemaining()) {
            try {
                out.write(buffer.get());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        if (this.remaining > 0) {
            buffer.clear();
            try {
                this.context.channel().read(buffer);
            } catch (ChannelException ex) {
                this.context.handleError(ex);
            }
        } else {
            synchronized (loadedFlag) {
                loadedFlag.set(true);
                loadedFlag.notifyAll();
            }
        }
    }
    
    private void decodeEntityBody(){
        loadEntityBody();
        if(decodeFlag.get()){
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
                new HttpMultipartDecoder().decode(this);
            } catch (Exception ex) {
                throw new InvalidOperationException(ex);
            }
        }else{
            throw new UnsupportedOperationException("Not supported decoding.");
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
        return hasEntityBody && !decodeFlag.get();
    }

    @Override
    public void loadEntityBody() throws InvalidOperationException {
        if (!canLoadEntityBody() || loadedFlag.get()) {
            return;
        }
        
        if(out==null){
            out=new TempOutputStream();
        }
        
        writeEntityBody(context.keepBuffer);

        synchronized (loadedFlag) {
            if (!loadedFlag.get()) {
                try {
                    loadedFlag.wait();
                } catch (InterruptedException ex) {
                    throw new InvalidOperationException(ex);
                }
            }
        }
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
        form.put(name, value);
    }

    @Override
    public void notifyDone() {
        synchronized (loadedFlag) {
            this.loadedFlag.set(true);
            this.loadedFlag.notify();
        }
    }

    @Override
    public Charset getEncoding() {
        return mano.io.CharsetUtil.UTF8;
    }

}
