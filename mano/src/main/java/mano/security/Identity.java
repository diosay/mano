/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.security;

import java.io.Serializable;

/**
 * 表示一个主体标识。
 *
 * @author jun
 */
public interface Identity {

    /**
     * @return 返回标识的值。
     */
    Serializable value();

    /**
     * @return 返回认证的类型，未认证应返回 null。
     */
    String getAuthenticationType();

    /**
     * @return 判断当前标识是否已经认证。
     */
    boolean isAuthenticated();

    /**
     * @return 返回当前标识的 String 值。
     */
    default String stringValue() {
        try {
            return value().toString();
        } catch (Throwable ex) {
            return null;
        }
    }
    
    /**
     * @return 返回当前标识的 Long 值，转换失败时返回 -1。
     */
    default long longValue() {
        try {
            if(value()!=null && (value() instanceof Long) || value() instanceof Integer){
                return (Long)value();
            }
            return Long.parseLong(value().toString());
        } catch (Throwable ex) {
            return -1;
        }
    }
    
    /**
     * @return 返回当前标识的 Integer 值，转换失败时返回 -1。
     */
    default int intValue() {
        try {
            if(value()!=null && value() instanceof Integer){
                return (Integer)value();
            }
            return Integer.parseInt(value().toString());
        } catch (Throwable ex) {
            return -1;
        }
    }
    
    /**
     * 转换为指定类型。
     * @param <T> 返回的类型。
     * @param type 返回的类型类。
     * @return 
     */
    default <T extends Identity> T cast(Class<T> type){
        if(type==null){
            throw new java.lang.NullPointerException("type");
        }
        return (T)this;
    }
    
}
