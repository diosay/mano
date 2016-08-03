/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.service;

import java.util.Properties;

/**
 * 表示一个运行时服务。
 * @author junhwong
 */
public interface Service {
    
    /**
     * 获取与此实例关联的配置属性。
     * @return 
     */
    Properties getProperties();
    
    /**
     * 获取当前的唯一服务名称。
     * @return 
     */
    String getName();
    
    /**
     * 在启动之前初始化该服务。
     */
    void init() throws Exception;
    
}
