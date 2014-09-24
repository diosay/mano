/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP 请求方法。
 *
 * @author jun <jun@diosay.com>
 */
public final class HttpMethod implements Comparable<HttpMethod> {

    /**
     * OPTIONS 方法。
     */
    public static final HttpMethod OPTIONS = new HttpMethod("OPTIONS");

    /**
     * GET 方法。
     */
    public static final HttpMethod GET = new HttpMethod("GET");

    /**
     * HEAD 方法。
     */
    public static final HttpMethod HEAD = new HttpMethod("HEAD");

    /**
     * POST 方法。
     */
    public static final HttpMethod POST = new HttpMethod("POST");

    /**
     * PUT 方法。
     */
    public static final HttpMethod PUT = new HttpMethod("PUT");

    /**
     * PATCH 方法。
     */
    public static final HttpMethod PATCH = new HttpMethod("PATCH");

    /**
     * DELETE 方法。
     */
    public static final HttpMethod DELETE = new HttpMethod("DELETE");

    /**
     * TRACE 方法。
     */
    public static final HttpMethod TRACE = new HttpMethod("TRACE");

    /**
     * CONNECT 方法。
     */
    public static final HttpMethod CONNECT = new HttpMethod("CONNECT");

    private static final Map<String, HttpMethod> methodMap = new HashMap<>();

    static {
        methodMap.put(OPTIONS.name, OPTIONS);
        methodMap.put(GET.name, GET);
        methodMap.put(HEAD.name, HEAD);
        methodMap.put(POST.name, POST);
        methodMap.put(PUT.name, PUT);
        methodMap.put(PATCH.name, PATCH);
        methodMap.put(DELETE.name, DELETE);
        methodMap.put(TRACE.name, TRACE);
        methodMap.put(CONNECT.name, CONNECT);
    }

    /**
     * 根据值获取 HttpMethod 实例。
     * @param name
     * @return 
     */
    public static HttpMethod valueOf(String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }

        name = name.trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("empty name");
        }

        HttpMethod result = methodMap.get(name);
        if (result != null) {
            return result;
        } else {
            return new HttpMethod(name.toUpperCase());
        }
    }

    private String name;

    private HttpMethod(String name) {
        this.name = name;
    }

    /**
     * 获取当前方法名称。
     * @return 
     */
    public String name() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj instanceof HttpMethod) {
            return name.equals(((HttpMethod) obj).name);
        }
        return name.equals(obj);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(HttpMethod o) {
        return name.compareTo(o.name);
    }
}
