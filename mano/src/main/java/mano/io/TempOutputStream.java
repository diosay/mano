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
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 *
 * @author jun
 */
public class TempOutputStream extends OutputStream {

    private int switchSize = 1024 * 16;
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
//            if (keep != null) {
//                while (keep.hasRemaining()) {
//                    channel.write(keep);
//                }
//                keep.clear();
//            }
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
                tmpfile = File.createTempFile("req_body", ".tmp");//TODO://, new File("D:\\tmp"
                fileStream = new FileOutputStream(tmpfile);
                tmpfile.deleteOnExit();
                isArray = false;
                arrayStream.flush();
                fileStream.write(arrayStream.toByteArray());
                //channel=new java.io.RandomAccessFile(tmpfile, "rw").getChannel();
                //channel.write(ByteBuffer.wrap(arrayStream.toByteArray()));
                arrayStream.close();
                arrayStream = null;
                
            }
        } else {
            fileStream.write(b);
//            if(keep!=null && !keep.hasRemaining()){
//                this.flush();
//            }
//            if(keep==null){
//                keep=ByteBuffer.allocate(1024);
//            }
//            keep.put((byte)b);
        }
    }
    ByteBuffer keep;
    FileChannel channel;

    public void write(ByteBuffer buf) throws IOException {
        if (isArray) {
            while (buf.hasRemaining()) {
                write(buf.get());
            }
        } else {
            
            while (buf.hasRemaining()) {
                if(buf.hasArray()){
                    fileStream.write(buf.array(), buf.arrayOffset()+buf.position(), buf.remaining());
                    buf.position(buf.position()+buf.remaining());
                }
                else{
                    fileStream.write(buf.get());
                }
            }
//            if (keep != null && keep.hasRemaining()) {
//                while (keep.hasRemaining()) {
//                    channel.write(keep);
//                }
//                keep.clear();
//            }
//            while (buf.hasRemaining()) {
//                channel.write(buf);
//            }
            
        }
    }

    @Override
    public void close() throws IOException {
        try {
            if (isArray) {
                arrayStream.close();
            } else {
                keep=null;
                fileStream.close();
                //channel.close();
                tmpfile.delete();
            }
        } finally {
            arrayStream = null;
            fileStream = null;
            tmpfile = null;
        }
    }

    private void checkClosed() throws IOException {
        if (fileStream == null && arrayStream == null) {
            throw new IOException("This Stream has been closed.");
        }
    }

    
    public InputStream toInputStream() throws IOException {
        this.flush();
        InputStream in;
        if (isArray) {
            in = new java.io.ByteArrayInputStream(this.arrayStream.toByteArray());
        } else {
            //fileStream.close();
            //channel.close();
            in = new java.io.FileInputStream(tmpfile);
        }
        return in;
    }
    
    protected void finalize() throws Throwable{
        try {
            this.close();
        } catch (IOException ex) {
            
        }
        super.finalize();
    }

}
