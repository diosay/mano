/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.web;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import mano.net.http.HttpContext;
import mano.util.NameValueCollection;

/**
 * 表示一个请求服务帮助类。
 *
 * @author jun <jun@diosay.com>
 */
public class ViewContext {

    private final Map<String, Object> viewbag;
    private final HttpContext context;
    private ActionResult result;
    private String path;
    private String controller;
    private String action;
    private Charset encoding=Charset.forName("utf-8");

    public ViewContext(HttpContext c) {
        if (c == null) {
            throw new IllegalArgumentException();
        }
        this.context = c;
        viewbag = new NameValueCollection<>();
    }
    
    /**
     * 设置输出编码。
     * @param charset 
     */
    public void setEncoding(Charset charset){
        if(charset==null){
            throw new IllegalArgumentException("charset");
        }
        this.encoding=charset;
    }
    
    /**
     * 设置输出编码。
     * @param charset 
     */
    public void setEncoding(String charset){
        this.setEncoding(Charset.forName(charset));
    }
    
    /**
     * 获取输出编码。
     * @return 
     */
    public Charset getEncoding(){
        return encoding;
    }

    /**
     * 设置当前控制器名称。
     * @param s 
     */
    public void setController(String s) {
        controller = s;
    }

    /**
     * 设置当前Action名称。
     * @param s 
     */
    public void setAction(String s) {
        action = s;
    }

    /**
     * 设置
     * @param s 
     */
    public void setPath(String s) {
        path = s;
    }

    public String getController() {
        return controller;
    }

    public String getAction() {
        return action;
    }

    public String getPath() {
        return path;
    }

    public Set<Entry<String, Object>> getEntries() {
        return this.viewbag.entrySet();
    }

    /**
     * 根据键获取视图字典的值。
     *
     * @param key
     * @return
     */
    public Object get(String key) {
        return viewbag.get(key);
    }

    /**
     * 设置一个视图的项。
     *
     * @param key
     * @param value
     */
    public void set(String key, Object value) {
        viewbag.put(key, value);
    }

    /**
     * 获取与当前服务关联的 HTTP 上下文。
     *
     * @return
     */
    public HttpContext getContext() {
        return this.context;
    }

    /**
     * 获取通过 setResult 的 action 结果。 如果未设置，则返回 null.
     *
     * @return
     */
    public ActionResult getResult() {
        return result;
    }

    public String getRequestPath() {
        return path;
    }

    /**
     * 设置 action 的处理结果。
     *
     * @param r
     */
    public void setResult(ActionResult r) {
        this.result = r;
    }
}
