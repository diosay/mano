/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano;

/**
 * 当方法调用对于对象的当前状态无效时引发的异常。
 * @author jun <jun@diosay.com>
 */
public class InvalidOperationException extends RuntimeException {
    public InvalidOperationException(){
        super();
    }
    
    public InvalidOperationException(String message) {
        super(message);
    }
    
    public InvalidOperationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public InvalidOperationException(Throwable cause) {
        super(cause);
    }
}
