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
import mano.http.HttpMethod;

/**
 *
 * @author jun <jun@diosay.com>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface UrlMapping {

    /**
     * 获取或设置请求的URL映射。 如：/index/{var}/
     *
     * @return
     */
    String value() default "";

    /**
     * 获取或设置用于请求的HTTP限定方法。
     *
     * @return
     */
    HttpMethod verb() default HttpMethod.ALL;
}
