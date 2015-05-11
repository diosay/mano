/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author jun
 */
public class TempOutputStream extends OutputStream {

    private int switchSize = 1024 * 8;
    private boolean isArray;
    private ByteArrayOutputStream arrayStream;
    private FileOutputStream fileStream;

    public TempOutputStream() {
        arrayStream = new ByteArrayOutputStream();
        isArray = true;
    }

    @Override
    public void flush() throws IOException {
        checkClosed();
        if (isArray) {
            arrayStream.flush();
        } else {
            fileStream.flush();
        }
    }
    File tmpfile;

    @Override
    public void write(int b) throws IOException {
        checkClosed();
        if (isArray) {
            arrayStream.write(b);
            if (arrayStream.size() >= switchSize) {
                tmpfile = File.createTempFile("req_body", ".tmp", new File("D:\\tmp"));
                fileStream = new FileOutputStream(tmpfile);
                tmpfile.deleteOnExit();
                isArray = false;
                arrayStream.flush();
                fileStream.write(arrayStream.toByteArray());
                arrayStream.close();
                arrayStream = null;
            }
        } else {
            fileStream.write(b);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            if (isArray) {
                arrayStream.close();
            } else {
                fileStream.close();
                tmpfile.delete();
            }
        } finally {
            arrayStream = null;
            fileStream = null;
            tmpfile = null;
        }
    }
    
    private void checkClosed() throws IOException{
        if(arrayStream==null && fileStream==null){
            throw new IOException("This Stream has been closed.");
        }
    }

    public InputStream toInputStream() throws IOException {
        this.flush();
        InputStream in;
        if (isArray) {
            in = new java.io.ByteArrayInputStream(this.arrayStream.toByteArray());
        } else {
            in = new java.io.FileInputStream(tmpfile);
        }
        return in;
    }

}
