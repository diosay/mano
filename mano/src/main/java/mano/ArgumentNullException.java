/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano;

/**
 * 当将空引用传递给不接受它作为有效参数的方法时引发的异常。
 * @author sixmoon
 */
public class ArgumentNullException extends IllegalArgumentException {

    public ArgumentNullException(String paramName) {
        super("The given parameter cannot be null. parameter name:" + paramName);
    }

    public ArgumentNullException(String paramName, Throwable cause) {
        super("The given parameter cannot be null. parameter name:" + paramName, cause);
    }

    public ArgumentNullException(Throwable cause) {
        super(cause);
    }
}
