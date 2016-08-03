/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package com.diosay.mano.io;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * 表示一个通道缓冲区。
 *
 * @author jun <jun@diosay.com>
 */
public class ChannelBuffer {

    public final ByteBuffer buffer;

    public ChannelBuffer(byte[] array, int index, int length) {
        buffer = ByteBuffer.wrap(array, index, length);
    }

    public ChannelBuffer(byte[] array) {
        buffer = ByteBuffer.wrap(array, 0, array.length);
    }

    public ChannelBuffer(int capacity) {
        buffer = ByteBuffer.allocate(capacity);
    }
    
    public ChannelBuffer(ByteBuffer inner) {
        buffer = inner;
    }
    
    public byte[] array(){
        return buffer.array();
    }
    
    public int offset(){
        return buffer.arrayOffset();
    }
    
    /**
     * 获取缓冲区的容量。
     * @return 
     */
    public int capacity(){
        return buffer.capacity();
    }
    
    /**
     * 当前缓冲的位置。
     * @return 
     */
    public int position(){
        return buffer.position();
    }
    
    /**
     * 设置新的缓冲区偏移。
     * @param pos 
     */
    public void position(int pos){
        buffer.position(pos);
    }
    
    /**
     * 获取有效数据大小。
     * @return 
     */
    public int limit(){
        return buffer.limit();
    }
    
    /**
     * 获取剩余数量
     * @return 
     */
    public int remaining(){
        return buffer.remaining();
    }
    
    /**
     * 缓冲区是否已满。
     * @return 
     */
    public boolean isFull(){
        return this.position()==0 && !this.hasRemaining();
    }
    
    /**
     * 缓冲区是否还有数据。
     * @return 
     */
    public boolean hasRemaining(){
        return buffer.hasRemaining();
    }
    
    /**
     * 清空缓冲区。
     */
    public void clear(){
        buffer.clear();
    }
    
    /**
     * 将缓冲区数据对齐到开始位置。
     */
    public void compact(){
        buffer.compact();
    }
    
    public static int bytesIndexOf(byte[] haystack, int index, int count, byte[] needle) {
        return bytesIndexOf(haystack, index, count, needle, 0, -1);
    }

    public static int bytesIndexOf(byte[] haystack, int index, int count, byte[] needle, int needleIndex, int needleCount) {
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
    
    public synchronized String readln(String charset) throws UnsupportedEncodingException {
        return readln(Charset.forName(charset));
    }

    private static byte[] CRLF = new byte[]{'\r', '\n'};
    
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
