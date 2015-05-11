/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.io;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 *
 * @author sixmoon
 */
public class BufferUtil {

    public static final byte[] CRLF = new byte[]{'\r', '\n'};

    public static int bytesIndexOf(byte[] haystack, int index, int count, byte[] needle) {
        return bytesIndexOf(haystack, index, count, needle, 0, needle == null ? 0 : needle.length);
    }

    public static int bytesIndexOfOL(byte[] haystack, int index, int count, byte[] needle, int needleIndex, int needleCount) {
        if (index + count > haystack.length) {
            return -1;
        }

        if (needleCount == -1) {
            needleCount = needle.length;
        }

        if (count < needleCount || needleIndex + needleCount > needle.length) {
            return -1;
        }

        for (int i = index; i < count + index; i++) {
            if (haystack[i] == needle[needleIndex]) //找到第一匹配的位置
            {
                for (int j = 0; j < needleCount - 1; j++) //连续匹配
                {
                    if (i + j >= count + index || haystack[i + j] != needle[j + needleIndex]) //如果中途不匹配
                    {
                        return bytesIndexOf(haystack, i + 1, count - (i - index) - 1, needle, needleIndex, needleCount);//从不匹配位置回溯
                    }
                }
                return i;
            }
        }

        return -1;
    }
    
    public static int bytesIndexOf(byte[] haystack, int index, int count, byte[] needle, int needleIndex, int needleCount) {
        if (haystack == null || needle == null) {
            return -1;
        } else if (index + count > haystack.length || needleIndex + needleCount > needle.length || count < needleCount) {
            return -1;
        }
        int pos=count+index;
        for (int i = index; i < pos; i++) {
            if (haystack[i] == needle[needleIndex]) //找到第一匹配的位置
            {
                for (int j = 0; j < needleCount; j++) //连续匹配
                {
                    if(i+j>=pos){
                        return -1;//位数不够匹配
                    }
                    else if (haystack[i + j] != needle[j + needleIndex]) //如果中途不匹配
                    {
                        return bytesIndexOf(haystack, i + 1, pos-i-1, needle, needleIndex, needleCount);//从不匹配位置回溯
                    }
                }
                return i;
            }
        }

        return -1;
    }
    
    
    private ByteBuffer buffer;
    byte[] array;
    int offset;
    int capacity;
    public BufferUtil setBuffer(ByteBuffer buffer){
        this.buffer=buffer;
        
        if(this.buffer.hasArray()){
            this.array=buffer.array();
            this.offset=buffer.arrayOffset();
            this.capacity=buffer.capacity();
        }else{
            this.array=new byte[buffer.capacity()];
            this.offset=0;
            this.capacity=this.array.length;
            this.buffer.mark();
            this.buffer.get(this.array);
            this.buffer.reset();
        }
        return this;
    }
    
    
    public byte[] array(){
        return array;
    }
    
    public int offset(){
        return offset;
    }
    
    /**
     * 获取缓冲区的容量。
     * @return 
     */
    public int capacity(){
        return capacity;
    }
    
    public synchronized String readln(String charset) throws UnsupportedEncodingException {
        return readln(Charset.forName(charset));
    }
    
     public synchronized String readln(Charset charset) {
        int off = buffer.arrayOffset() + buffer.position();
        if (buffer.limit() < CRLF.length) {
            return null;
        }
        int index = bytesIndexOf(array(), off, buffer.remaining(), CRLF);
        if (index < 0) {
            return null;
        }
        String result = readstr(off, index - off, charset);
        buffer.position(buffer.position() + CRLF.length);
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
