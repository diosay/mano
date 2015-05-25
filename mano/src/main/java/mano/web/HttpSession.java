/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.web;

import java.util.HashMap;
import java.util.UUID;
import mano.caching.CacheProvider;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class HttpSession {
    
    /**
     * 提供存储 Session 的提供程序。
     */
    protected final CacheProvider provider;
    private String sid;
    private boolean isnew;
    private boolean abandoned;
    public static final String COOKIE_KEY = "MANOSESSIONID";
    private int timeout=1000 * 60 * 20;
    protected HttpSession(CacheProvider provider) {
        this(Integer.toHexString(UUID.randomUUID().hashCode()), provider);
        isnew = true;
        this.provider.set(sid, new HashMap<>(), timeout, true, null);
    }
    
    protected HttpSession(String sessionId, CacheProvider provider) {
        this.sid = sessionId;
        this.provider = provider;
    }
    
    public static HttpSession getSession(String sessionId, CacheProvider provider) {
        if (sessionId == null || "".equals(sessionId) || !provider.contains(sessionId)) {
            return new HttpSession(provider);
        }
        return new HttpSession(sessionId, provider);
    }
    
    public final String getSessionId() {
        return sid;
    }
    
    public final boolean isNewSession() {
        return isnew;
    }
    public final boolean isAbandoned(){
        return abandoned;
    }
    
    public void set(String name, Object val) {
        this.provider.set(sid, name, val);
    }
    
    public Object get(String name) {
        return this.provider.get(sid, name);
    }
    
    public void remove(String name) {
        this.provider.remove(sid, name);
    }
    
    public final void abandon(){
        abandoned=true;
        doAbandon();
    }
    
    protected void doAbandon(){
        
    }
    
}
