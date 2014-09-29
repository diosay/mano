/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util.xml;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class XmlException extends Exception{
    public XmlException(){
        super();
    }
    
    public XmlException(String message) {
        super(message);
    }
    
    public XmlException(String message, Throwable cause) {
        super(message, cause);
    }
}
