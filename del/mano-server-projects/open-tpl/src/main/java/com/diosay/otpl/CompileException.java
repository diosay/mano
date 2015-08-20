/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */

package com.diosay.otpl;

/**
 * 编译异常。
 * @author jun <jun@diosay.com>
 */
public class CompileException extends RuntimeException {
    public CompileException(){
        super();
    }
    
    public CompileException(String message) {
        super(message);
    }
    
    public CompileException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public CompileException(Throwable cause) {
        super(cause);
    }
}
