/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

import mano.web.WebApplication;

/**
 *
 * @author jun <jun@diosay.com>
 */
public abstract class HttpContext {
    /*private HttpModule _handler;
    private WebApplication _application;
    public HttpModule handler(){
        return _handler;
    }
    
    public void handler(HttpModule handler){
        _handler=handler;
    }
    
    public void setApplication(WebApplication application){
        _application=application;
    }
    */
    public abstract WebApplication application();
    
    
    public abstract boolean isCompleted();
    
    //public abstract boolean complete();
    
    public abstract HttpRequest request();

    public abstract HttpResponse response();
    
    public abstract HttpServer server();
}
