/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

/**
 * 描述在处理 HTTP 请求期间发生的异常。
 *
 * @author jun <jun@diosay.com>
 */
public class HttpException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private HttpStatus httpStatus;

    public HttpException(HttpStatus status) {
        super();
        httpStatus = status;
    }
    
    public HttpException(HttpStatus status, String message) {
        super(message);
        httpStatus = status;
    }
    
    public HttpException(HttpStatus status, Throwable t) {
        super(t);
        httpStatus = status;
    }
    public HttpException(Throwable t) {
        super(t);
        httpStatus = HttpStatus.InternalServerError;
    }

    /*
     * 获取要返回给客户端的 HTTP 响应状态代码。
     */
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

}
