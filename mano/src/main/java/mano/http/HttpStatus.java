/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.reflect.Field;

/**
 * HHTP 规范定义的响应状态。
 *
 * @author jun <jun@diosay.com>
 */
public enum HttpStatus {

    Continue(100, "Continue"),
    SwitchingProtocols(101, "Switching Protocols"),
    OK(200, "OK"),
    Created(201, "Created"),
    Accepted(202, "Accepted"),
    NonAuthoritativeInformation(203, "Non-Authoritative Information"),
    NoContent(204, "No Content"),
    ResetContent(205, "Reset Content"),
    PartialContent(206, "Partial Content"),
    MultipleChoices(300, "Multiple Choices"),
    MovedPermanently(301, "Moved Permanently"),
    Found(302, "Found"),
    SeeOther(303, "See Other"),
    NotModified(304, "Not Modified"),
    UseProxy(305, "Use Proxy"),
    TemporaryRedirect(307, "Temporary Redirect"),
    BadRequest(400, "Bad Request"),
    Unauthorized(401, "Unauthorized"),
    Forbidden(403, "Forbidden"),
    NotFound(404, "Not Found"),
    MethodNotAllowed(405, "Method Not Allowed"),
    NotAcceptable(406, "Not Acceptable"),
    ProxyAuthenticationRequired(407, "Proxy Authentication Required"),
    RequestTimeout(408, "Request Timeout"),
    Conflict(409, "Conflict"),
    Gone(410, "Gone"),
    LengthRequired(411, "Length Required"),
    PreconditionFailed(412, "Precondition Failed"),
    RequestEntityTooLarge(413, "Request Entity Too Large"),
    RequestURITooLong(414, "Request URI Too Long"),
    RequestedRangeNotSatisfiable(416, "Requested Range Not Satisfiable"),
    InternalServerError(500, "Internal Server Error"),
    NotImplemented(501, "Not Implemented"),
    BadGateway(502, "Bad Gateway"),
    ServiceUnavailable(503, "Service Unavailable"),
    GatewayTimeout(504, "Gateway Timeout"),
    HTTPVersionNotSupported(505, "HTTP Version Not Supported"),
    Unknown(0, "Unknown");
    private String description;
    private int status;

    private HttpStatus(int status,String desc){
        this.status=status;
        this.description=desc;
    }
    
    /**
     * 获取描述。
     * @return 
     */
    public String getDescription(){
        return this.description;
    }
    
    /**
     * 获取状态值。
     * @return 
     */
    public int getStatus(){
        return this.status;
    }
    
    /**
     * 获取值。
     * @param status
     * @param desc
     * @return 
     */
    public static HttpStatus valueOf(int status,String desc){
        for(HttpStatus item:HttpStatus.values()){
            if(item.getStatus()==status){
                return item;
            }
        }
        HttpStatus result=HttpStatus.Unknown;
        result.description=desc;
        result.status=status;
        return result;
    }
    
}
