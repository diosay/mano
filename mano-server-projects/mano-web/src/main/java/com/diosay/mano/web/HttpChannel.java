/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package com.diosay.mano.web;

import com.diosay.mano.io.AioSocketChannel;
import com.diosay.mano.io.ChannelBuffer;
import com.diosay.mano.io.ChannelHanlder;
import com.diosay.mano.io.Listener;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.Charset;
import mano.caching.CacheProvider;
import mano.http.HttpContext;
import mano.http.HttpRequest;
import mano.http.HttpResponse;
import mano.http.HttpServer;
import mano.service.Service;
import mano.service.ServiceManager;
import mano.service.ServiceProvider;
import mano.web.HttpSession;
import mano.web.WebApplication;
import mano.web.WebApplicationStartupInfo;

/**
 * HTTP协议通道。
 *
 * @author jun <jun@diosay.com>
 */
public class HttpChannel extends AioSocketChannel implements HttpContext {

    HttpChannel(AsynchronousSocketChannel channel, Listener listener) {
        super(channel, listener);
    }

    Charset inputEncoding = Charset.forName("utf-8");

    /**
     * 获取输入编码。
     *
     * @return
     */
    public Charset getInputEncoding() {
        return inputEncoding;
    }

    public static final int IDLE = 0;
    public static final int REQUEST_LINE = 1;
    public static final int LOAD_HEADER = 2;
    public static final int RESPONSE = 3;
    int phase;
    //ChannelHanlder<? extends HttpChannel> handler;
    ChannelBuffer buffer;
    HttpRequestImpl request;
    HttpResponseImpl response;
    HttpServer server;
    WebApplication application;
    HttpSession session;
    boolean isCompleted;

    void start() {
        buffer.clear();
        request = null;
        phase = REQUEST_LINE;
    }

    boolean postResponse() {
        HttpService service = (HttpService) this.getListener().getGroup();
        String host = request.headers.get("Host").value();
        WebApplicationStartupInfo info = null;

        for (WebApplicationStartupInfo i : service.appInfos.values()) {
            if (i.matchHost(host)) {
                info = i;
                break;
            }
        }

        if (info == null) {
            service.appInfos.get("*");
        }

        if (info == null) {

//            response = new HttpResponseImpl();
//            response.channel = this;
//
//            response.write("hello");
//            response.end();
            return true;
        }
        info.serviceLoader=((HttpService)this.getListener().getGroup()).loader;
        info.service=(com.diosay.mano.service.Service)this.getListener().getGroup();
        WebApplication app = info.getInstance();
        if (app == null) {
            return false;
        }
        application = app;
        server = info.getServerInstance();
        response = new HttpResponseImpl();
        response.channel = this;

        //build session
        Service svc = ServiceManager.getInstance().getService("cache.service");
        if (svc != null && svc instanceof ServiceProvider) {
            CacheProvider provider = ((ServiceProvider) svc).getService(CacheProvider.class);//TODO: 指定实例服务
            if (provider != null) {
                    String sid = request.getCookie().get(HttpSession.COOKIE_KEY);
                    session = HttpSession.getSession(sid, provider);

                    if (session.isNewSession()) {
                        //req.url().getHost()
                        response.getCookie().set(HttpSession.COOKIE_KEY, session.getSessionId(), 0, "/", null, false, false);
                    }
            } else {
                //logger.error("CacheProvider not found.");
            }
        } else {
            //logger.error("cache.service not found.");
        }
        response.setHeader("Server", server.getVersion());
        //context.response.setHeader("X-Powered-By", "mano/1.1,java/1.8");

        app.processRequest(this);

        return true;
    }

    @Override
    public WebApplication getApplication() {
        return this.application;
    }

    @Override
    public boolean isCompleted() {
        return isCompleted;
    }

    @Override
    public HttpRequest getRequest() {
        return request;
    }

    @Override
    public HttpResponse getResponse() {
        return response;
    }

    @Override
    public HttpServer getServer() {
        return server;
    }

    @Override
    public HttpSession getSession() {
        return session;
    }

}
