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
 * 一个写入过滤器，可以在写入时重定向输出或改变编码等。
 * @author jun <jun@diosay.com>
 */
public interface Filter {
     /**
      * 写入二进制。
      * @param writer
      * @param original
      * @param array
      * @param index
      * @param count 
      */
    void write(Writer writer,Charset original, byte[] array, int index, int count);
}
