/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.security;

/**
 *
 * @author jun
 */
public class LicenseValidationException extends RuntimeException{
        public LicenseValidationException(){
            this(null,null);
        }
        public LicenseValidationException(String message){
            this(message,null);
        }
        public LicenseValidationException(Throwable cause){
            this(cause.getMessage(),null);
        }
        public LicenseValidationException(String message, Throwable cause){
            super(message,cause,false,false);
        }
        
    }
