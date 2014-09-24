/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */

package com.diosay.mano.service;

import java.util.Properties;

/**
 * 定义表示一个服务。
 * @author jun <jun@diosay.com>
 */
public interface Service extends Runnable {
    
    /**
     * 获取服务配置属性。
     * @return 
     */
    Properties getProperties();
    
    /**
     * 获取服务名称。
     * @return 
     */
    String getName();
    
    /**
     * 初始化服务。
     */
    void init() throws Exception;
}
