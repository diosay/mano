/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.web;

import mano.logging.Logger;
import mano.net.http.HttpContext;
import mano.net.http.HttpPostFile;

/**
 * 表示一个 WEB 应用模块。
 * <p>实现类必须是以 {@code Module} 结束。如：新闻模块 NewsModule
 * @author jun
 */
public abstract class Module {
    
    private ViewContext context;
    
    /**
     * 执行该实例的初始化工作。
     * @param context {@link ViewContext}
     */
    final void init(ViewContext context){
        if(context==null){
            throw new java.lang.NullPointerException("context");
        }
        this.context=context;
    }
    
    /**
     * 返回与请求关联的 {@link HttpContext} 对象。
     * @return {@link HttpContext}
     */
    public final HttpContext context(){
        return context.getContext();
    }
    
    /**
     * 返回当前请求所关联的应用程序日志器。
     * @return {@link mano.web.WebApplication#getLoader()}。
     */
    protected final Logger logger(){
        return context().getApplication().getLogger();
    }

    /**
     * 根据 {@code key} 获取当前请求 {@link mano.net.http.HttpRequest#url()} 的查询参数值。
     * @param key 键名。
     * @return 返回对应的值，如果未找到返回 {@code null}。
     */
    protected String getQuery(String key){
        return context().getRequest().query().getOrDefault(key, null);
    }
    
    /**
     * 根据 {@code key} 获取当前请求所提交的表单内容。
     * @param key 表单字段名称。
     * @return 返回对应的值，如果未找到返回 {@code null}。
     */
    protected String getForm(String key){
        return context().getRequest().form().getOrDefault(key, null);
    }
    
    /**
     * 根据 {@code key} 获取当前请求所上传的文件。
     * @param key 表单字段名称。
     * @return 返回对应的{@link HttpPostFile}实例，如果未找到返回 {@code null}。
     */
    protected HttpPostFile getFile(String key){
        return context().getRequest().files().getOrDefault(key, null);
    }
    
    /**
     * 根据 {@code key} 获取当前请求的浏览器 {@link mano.net.http.HttpCookieCollection} 的值。
     * @param key Cookie的名称。
     * @return 返回对应的值，如果未找到返回 {@code null}。
     */
    protected String getCookie(String key){
        return context().getRequest().cookies().get(key);
    }
    
    /**
     * 根据 {@code key} 获取当前请求所关联的服务器 Session 值。
     * @param key Session 名称。
     * @return 返回对应的值，如果未找到返回 {@code null}。
     */
    protected Object getSession(String key){
        return context().getSession().get(key);
    }
    
    /**
     * 设置一个浏览 Cookie，更多选择请使用 {@link mano.net.http.HttpResponse#cookie()} 。
     * @param key Cookie的名称。
     * @param value Cookie的值。注意：该对象在添加时会转换会{@code String}。
     * @return 所设置的值。
     */
    protected String setCookie(String key,Object value){
        return context().getResponse().cookies().set(key, value);
    }
    
    /**
     * 设置一个服务器 Session 。
     * @param key Session 名称。
     * @param value Session 对象。注意：该对象必须能被序列化。参见：{@link java.io.Serializable}。
     * @return 所设置的值。
     */
    protected Object setSession(String key,Object value){
        context().getSession().set(key, value);
        return value;
    }
    
    
    /**
     * 设置一个默认 视图结果。
     */
    protected ActionResult view() {
        return view(null, null);
    }

    protected ActionResult view(String action) {
        return view(action, null);
    }

    protected ActionResult view(String action, String controller) {
        if (action != null) {
            context.setAction(action);
        }
        if (controller != null) {
            context.setController(controller);
        }
        context.setResult(new ViewResult());
        return context.getResult();
    }

    protected ActionResult template(String path) {
        context.setPath(path);
        context.setResult(new ViewResult());
        return context.getResult();
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
        context().getResponse().setContentType(contentType);
        context().getResponse().write(content);
    }
    
    void t(){
        
    }
    
    
}
