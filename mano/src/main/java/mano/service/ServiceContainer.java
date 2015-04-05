/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.service;

/**
 *
 * @author johnwhang
 */
public interface ServiceContainer {
    void add(Service service);
    void remove(Service service);
    /**
     * 
     */
    ServiceContainer run();
    /**
     * 等待所有服务运行结束。
     * 该方法将自动启动未运行的服务。
     */
    void await();
}
