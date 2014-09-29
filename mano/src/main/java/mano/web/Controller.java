/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.web;

import mano.ContextClassLoader;
import mano.http.HttpContext;
import mano.http.HttpPostFile;
import mano.http.HttpRequest;
import mano.http.HttpResponse;
import mano.util.json.JsonConvert;
import mano.util.json.JsonConverter;
import mano.util.logging.Logger;

/**
 * 封装了一组 action 与常用方法的控制器抽象类。
 *
 * @author jun <jun@diosay.com>
 */
public abstract class Controller {

    //private HttpResponse response;
    //private HttpRequest request;
    private HttpContext context;
    private ViewContext service;
    private JsonConverter jsonConverter;

    private final void setService(ViewContext rs) {
        service = rs;
        service.setEncoding(rs.getContext().getResponse().charset());
        context = rs.getContext();
    }

    private ViewContext getService() {
        if (this.service == null) {
            throw new mano.InvalidOperationException("未初始化服务。");
        }
        return this.service;
    }

    public void set(String name, Object value) {
        getService().set(name, value);
    }

    public Object get(String name) {
        return getService().get(name);
    }

    public String query(String name) {
        return getContext().getRequest().query().get(name);
    }

    public String form(String name) {
        if (getContext().getRequest().form() == null) {
            return null;
        }
        return getContext().getRequest().form().get(name);
    }

    public HttpPostFile file(String name) {
        if (getContext().getRequest().files() == null) {
            return null;
        }
        return getContext().getRequest().files().get(name);
    }

    public void session(String name, Object value) {
        getContext().getSession().set(name, value);
    }

    public Object session(String name) {
        try {
            return getContext().getSession().get(name);
        } catch (Throwable ex) {
            getLogger().debug(ex);
        }
        return null;

    }

    /**
     * 设置一个响应 cookie 。
     *
     * @param name
     * @param value
     */
    public void cookie(String name, Object value) {
        getContext().getResponse().getCookie().set(name, value);
    }

    /**
     * 获取一个请求 cookie。
     *
     * @param name
     * @return
     */
    public String cookie(String name) {
        return getContext().getRequest().getCookie().get(name);
    }

    /**
     * 设置一个默认 视图结果。
     */
    protected void view() {
        view(null,null);
    }

    protected void view(String action) {
        view(action,null);
    }

    protected void view(String action, String controller) {
        if (action != null) {
            getService().setAction(action);
        }
        if (controller != null) {
            getService().setController(controller);
        }
        getService().setResult(new ViewResult());
    }

    protected void template(String path) {
        getService().setPath(path);
        getService().setResult(new ViewResult());
    }

    protected void text(String content) {
        this.text(content, "text/plain;charset=utf-8");
    }

    /**
     * 将一个纯文本
     *
     * @param content
     * @param contentType
     */
    protected void text(String content, String contentType) {
        getContext().getResponse().setContentType(contentType);
        getContext().getResponse().write(content);
    }

    /**
     * 设置 JSON 转换程序。
     *
     * @param converter
     */
    protected final void setJsonConverter(JsonConverter converter) {
        jsonConverter = converter;
    }

    /**
     * 将一个对象转换为 JSON 格式，并设置到响应结果以待输出到客户端。
     *
     * @param src
     */
    protected void json(Object src) {
        if (jsonConverter == null) {
            jsonConverter = JsonConvert.getConverter(getLoader());

            if (jsonConverter == null) {
                throw new NullPointerException("Controller.json:Not found JsonConverter");
            }
        }

        getContext().getResponse().setContentType("application/json;charset=utf-8");
        getContext().getResponse().write(jsonConverter.serialize(src));
    }

    /**
     * 获取当前 HTTP 请求的上下文实例。
     *
     * @return
     */
    public HttpContext getContext() {
        return getService().getContext();
    }

    /**
     * 获取服务于当前 HTTP 请求的 Web 应用程序。
     *
     * @return
     */
    public WebApplication getApplication() {
        return getContext().getApplication();
    }

    /**
     * 获取与当前应用所关联的日志组件。
     *
     * @return
     */
    public Logger getLogger() {
        return this.getApplication().getLogger();
    }

    /**
     * 获取当前应用所关联的类加载器。
     *
     * @return
     */
    public ContextClassLoader getLoader() {
        return getApplication().getLoader();
    }

}
