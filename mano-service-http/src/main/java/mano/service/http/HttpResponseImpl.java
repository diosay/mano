/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.service.http;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import mano.ArgumentNullException;
import mano.InvalidOperationException;
import mano.net.http.HttpHeader;
import mano.net.http.HttpHeaderCollection;
import mano.net.http.HttpResponse;

/**
 *
 * @author sixmoon
 */
public class HttpResponseImpl extends HttpResponse {

    HttpHeaderCollection headers = new HttpHeaderCollection();

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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean headerSent() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isConnected() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    ByteBuffer contentBuffer;
    List<ByteBuffer> buffers=new ArrayList<>();
    @Override
    public void write(byte[] buffer, int offset, int count) {

        if (contentBuffer != null) {
            int len=Math.min(count, contentBuffer.remaining());
            contentBuffer.put(buffer, offset, len);
            if(!contentBuffer.hasRemaining()){
                contentBuffer.flip();
                buffers.add(contentBuffer);
                contentBuffer=null;
            }
            if(len-count<0){
                //TODO:contentBuffer=pool.get();
                write(buffer,offset+len,count-len);
                return;
            }
        }else{
            buffers.add(ByteBuffer.wrap(buffer, offset, count));
        }

        if (!this.buffering()) {
            this.flush();
        }
    }

    @Override
    public void transmit(String filename) throws FileNotFoundException, IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void transmit(String filename, long position, long length) throws FileNotFoundException, IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void flush() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void end() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
