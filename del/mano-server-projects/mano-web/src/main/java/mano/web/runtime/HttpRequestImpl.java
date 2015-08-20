/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.web.runtime;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import mano.InvalidOperationException;
import mano.http.HttpEntityBodyAppender;
import mano.http.HttpEntityBodyDecoder;
import mano.http.HttpFormUrlEncodedDecoder;
import mano.http.HttpHeaderCollection;
import mano.http.HttpMethod;
import mano.http.HttpMultipartDecoder;
import mano.http.HttpPostFile;
import mano.http.HttpRequest;
import mano.http.HttpVersion;
import mano.util.NameValueCollection;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class HttpRequestImpl extends HttpRequest implements HttpEntityBodyAppender {

    HttpMethod method;
    String rawUrl;
    HttpVersion version;
    HttpHeaderCollection headers = new HttpHeaderCollection();
    final AtomicBoolean postLoadedFlag = new AtomicBoolean(false);
    final AtomicBoolean loadedFlag = new AtomicBoolean(false);
    Map<String, String> form;
    Map<String, HttpPostFile> files;
    long contentLength;
    long remaining;
    boolean hasEntityBody;
    boolean isChunked;
    boolean isFormUrlEncoded;
    boolean isFormMultipart;
    String boundary;
    HttpChannel channel;
    HttpEntityBodyDecoder decoder;
    boolean pred;
    private synchronized void pre(){
        if(pred){
            return;
        }
        pred=true;
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
    
    private synchronized void doLoadEntityBody(boolean auto) {
        postLoadedFlag.set(true);
        
        if (isChunked) {
            throw new UnsupportedOperationException("Not supported chunked encoding.");
        } else if (auto && isFormUrlEncoded) {
            try {
                postLoadedFlag.set(false);
                loadEntityBody(new HttpFormUrlEncodedDecoder());
            } catch (Exception ex) {
                throw new InvalidOperationException(ex);
            }
        } else if (auto && isFormMultipart) {
            try {
                postLoadedFlag.set(false);
                loadEntityBody(new HttpMultipartDecoder());
            } catch (Exception ex) {
                throw new InvalidOperationException(ex);
            }
        }
    }

    private void waitLoadEntityBody() {
        try {
            synchronized (loadedFlag) {
                if (!loadedFlag.get()) {
                    loadedFlag.wait(1000 * 60 * 30);//30m
                }
            }
        } catch (InterruptedException ex) {
            throw new InvalidOperationException(ex);
        }
    }

    public boolean keepAlive() {
        return false;
    }

    @Override
    public String method() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String version() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String protocol() {
        throw new UnsupportedOperationException("Not supported yet.");
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
        return contentLength;
    }

    @Override
    public Map<String, String> form() {
        loadEntityBody();
        return form;
    }

    @Override
    public Map<String, HttpPostFile> files() {
        loadEntityBody();
        return files;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public boolean isConnected() {
        return channel.isOpen();
    }

    @Override
    public boolean canLoadEntityBody() {
        pre();
        return hasEntityBody && !postLoadedFlag.get();
    }

    @Override
    public void loadEntityBody(HttpEntityBodyDecoder decoder) throws Exception {
        if (!canLoadEntityBody()) {
            return;
        }
        doLoadEntityBody(false);
        postLoadedFlag.set(true);
        this.decoder = decoder;
        int pos = channel.buffer.position();
        if (channel.buffer.hasRemaining()) {
            decoder.onRead(channel.buffer, this);
            this.remaining -= channel.buffer.position() - pos;
        }
        
        if(loadedFlag.get() && (this.remaining >0 || channel.buffer.hasRemaining())){
            this.remaining-=channel.buffer.remaining();
            channel.buffer.position(channel.buffer.limit());//TODO
        }

        if (this.remaining > 0) {
            if (!channel.buffer.hasRemaining()) {
                channel.buffer.clear();
            } else {
                channel.buffer.compact();
            }
            channel.read(channel.buffer);
        }
        waitLoadEntityBody();
    }

    @Override
    public void loadEntityBody() throws InvalidOperationException {
        if (!canLoadEntityBody()) {
            return;
        }
        doLoadEntityBody(true);
    }

    @Override
    public void Abort() {
        channel.close();
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
        return channel.inputEncoding;
    }
}
