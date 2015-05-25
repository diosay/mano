/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.net.http;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP 请求方法。
 *
 * @author jun <jun@diosay.com>
 */
public enum HttpMethod implements Comparable<HttpMethod> {

    /**
     * OPTIONS 方法。
     */
    OPTIONS("OPTIONS"),

    /**
     * GET 方法。
     */
    GET("GET"),

    /**
     * HEAD 方法。
     */
    HEAD("HEAD"),

    /**
     * POST 方法。
     */
    POST("POST"),

    /**
     * PUT 方法。
     */
    PUT("PUT"),

    /**
     * PATCH 方法。
     */
    PATCH("PATCH"),

    /**
     * DELETE 方法。
     */
    DELETE("DELETE"),

    /**
     * TRACE 方法。
     */
    TRACE("TRACE"),

    /**
     * CONNECT 方法。
     */
    CONNECT("CONNECT"),
    /**
     * 所有。
     */
    ALL("ALL"),
    /**
     * 未知。
     */
    UNKNOWN("UNKNOWN");

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
        methodMap.put(ALL.name, ALL);
    }

    /**
     * 根据值获取 HttpMethod 实例。
     * @param name
     * @return 
     */
    public static HttpMethod parse(String name) {
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
            result=UNKNOWN;
            result.name=name.toUpperCase();
            return result;
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
    public String value() {
        return name;
    }

    /**
     * 比较对象。
     * @param obj
     * @return 
     */
    public boolean equalWith(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj instanceof HttpMethod) {
            return name.equalsIgnoreCase(((HttpMethod) obj).name);
        }
        return name.equals(obj);
    }

    @Override
    public String toString() {
        return name;
    }
}
