/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package com.diosay.otpl.runtime;

import java.nio.charset.Charset;

/**
 * 写入器
 *
 * @author jun <jun@diosay.com>
 */
public interface Writer {

    /**
     * 获取过滤器。
     * @return 
     */
    Filter getFilter();
    
    /**
     * 写入字序列。
     * @param filtrable
     * @param cs
     * @param start
     * @param end 
     */
    void write(boolean filtrable,CharSequence cs, int start, int end);

    /**
     * 写入二进制。
     * @param filtrable
     * @param original
     * @param array
     * @param index
     * @param count 
     */
    void write(boolean filtrable,Charset original, byte[] array, int index, int count);
}
