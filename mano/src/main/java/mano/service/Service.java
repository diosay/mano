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
 *
 * @author jun <jun@diosay.com>
 */
public abstract class Service implements com.diosay.mano.service.Service {

    private ServiceContainer container;
    private Properties properties;
    
    protected Service() {
        
        properties = new Properties();
        container = ServiceManager.getInstance();
    }

    public void init(ServiceContainer container, Map<String, String> params) {
        this.container = container;

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

    public ServiceContainer getContainer() {
        return container;
    }
    
    public void process(Intent intent) throws Exception{
        
    }

    @Override
    public void init() throws Exception {
    }
    
    public abstract String getServiceName();
    
    @Override
    public String getName(){
        return getServiceName();
    }

    @Override
    public String toString() {
        return this.getClass() + "[" + getServiceName() + "]";
    }
    
    
}
