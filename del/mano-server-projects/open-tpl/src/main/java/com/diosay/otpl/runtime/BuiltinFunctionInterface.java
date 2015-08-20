/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */

package com.diosay.otpl.runtime;

/**
 * 内建函数(BFI)。
 * @author jun <jun@diosay.com>
 */
public interface BuiltinFunctionInterface {
    
    /**
     * 将对象转换为字符串串并合并。
     * @param args
     * @return 
     */
    String str(Object[] args);
    Object iterator(Object obj);
    Object indexer(Object obj,Object[] args);
}
