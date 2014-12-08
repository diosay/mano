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
import mano.data.json.*;
import mano.util.logging.Logger;

/**
 * 封装了一组 action 与常用方法的控制器抽象类。
 *
 * @author junhwong
 */
public abstract class Controller implements ActionHandler {

    private ViewContext service;
    private JsonConverter jsonConverter;

    @Override
    public final void init(ViewContext context) {
        if (context == null) {
            throw new NullPointerException("context");
        }
        this.service = context;
        onInit();
    }

    @Override
    public final void dispose() {
        try {
            onDispose();
        } finally {
            service = null;
            jsonConverter = null;
        }
    }

    /**
     * 当初始化时调用。
     */
    protected void onInit() {

    }

    /**
     * 当释放资源时调用。
     */
    protected void onDispose() {

    }

    /**
     * 设置用于在当前请求传递的键值。
     *
     * @param name
     * @param value
     */
    public void set(String name, Object value) {
        service.set(name, value);
    }

    /**
     * 获取一个值。
     *
     * @param name
     * @return
     */
    public Object get(String name) {
        return service.get(name);
    }

    /**
     * 根据name获取一个当前请求所关联的参数。
     *
     * @param name
     * @return
     */
    public String query(String name) {
        return getContext().getRequest().query().get(name);
    }

    /**
     * 获取一个表单值。
     *
     * @param name
     * @return
     */
    public String form(String name) {
        if (getContext().getRequest().form() == null) {
            return null;
        }
        return getContext().getRequest().form().get(name);
    }

    /**
     * 获取一个上传文件。
     *
     * @param name
     * @return
     */
    public HttpPostFile file(String name) {
        if (getContext().getRequest().files() == null) {
            return null;
        }
        return getContext().getRequest().files().get(name);
    }

    /**
     * 设置一个 session。
     *
     * @param name
     * @param value
     */
    public void session(String name, Object value) {
        getContext().getSession().set(name, value);
    }

    /**
     * 获取一个session。
     *
     * @param name
     * @return
     */
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
        view(null, null);
    }

    protected void view(String action) {
        view(action, null);
    }

    protected void view(String action, String controller) {
        if (action != null) {
            service.setAction(action);
        }
        if (controller != null) {
            service.setController(controller);
        }
        service.setResult(new ViewResult());
    }

    protected void template(String path) {
        service.setPath(path);
        service.setResult(new ViewResult());
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
     * @deprecated
     */
    protected final void setJsonConverter(JsonConverter converter) {
        jsonConverter = converter;
    }

    /**
     *
     */
    public static String JSON_CONVERTER_APP_KEY = "__WEBAPP_JSON_CONVERTER";

    /**
     * 将一个对象转换为 JSON 格式，并设置到响应结果以待输出到客户端。
     *
     * @param src
     */
    protected void json(Object src) {
        if (jsonConverter == null) {
            jsonConverter = this.getApplication().get(JSON_CONVERTER_APP_KEY) == null ? null : (JsonConverter) this.getApplication().get(JSON_CONVERTER_APP_KEY);
            //   JsonConvert.getConverter(getLoader());

            if (jsonConverter == null) {
                throw new JsonException(new NullPointerException("Controller.json:Not found JsonConverter"));
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
        return service.getContext();
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
