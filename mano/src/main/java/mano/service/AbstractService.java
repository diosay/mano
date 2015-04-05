/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.service;

import java.util.Map;
import java.util.Properties;

/**
 * 运行时服务抽象类。
 *
 * @author junhwong
 */
public abstract class AbstractService implements IService, Runnable {

    private final ServiceContainerBase container;
    private final Properties properties;

    protected AbstractService() {
        properties = new Properties();
        container = ServiceManager.getInstance();
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String def) {
        return properties.getProperty(key, def);
    }

    public void stop() {

    }

    public void onStart() {
        ServiceManager.getInstance().regisiter(this);
    }

    public ServiceContainerBase getContainer() {
        return container;
    }

    public void process(Intent intent) throws Exception {

    }

    @Override
    public void init() throws Exception {
    }

    public abstract String getServiceName();

    @Override
    public String getName() {
        return getProperties().getProperty("service.name", "unnamed");
    }

    @Override
    public String toString() {
        return this.getClass() + "[" + getServiceName() + "]";
    }

    @Override
    protected void finalize() throws Throwable{
        this.stop();
        super.finalize();
    }
}
