/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.web;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个根据请求URL的缓存项。
 * @author jun
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface UrlCached {
    /**
     * 获取或设置一个用于管理该缓存的名称。
     *
     * @return
     */
    String value() default "";
    
    /**
     * 设置相对缓存时间。
     * @return 
     */
    long timeout() default -1;
    
    /**
     * 设置一个用于检查session是否空的依赖，如果设置该项并且session的值为空则缓存失效。
     * @return 
     */
    String sessionKey() default "";
}
