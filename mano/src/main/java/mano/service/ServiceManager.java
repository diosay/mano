/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import mano.ContextClassLoader;
import mano.util.logging.Logger;

/**
 * 全局的服务管理器。
 *
 * @author jun <jun@diosay.com>
 */
public final class ServiceManager implements ServiceContainer {

    private HashMap<String, Service> services = new HashMap<>();
    private ContextClassLoader classLoader;
    private static final ServiceManager instance;

    private ServiceManager() {
        //System.setSecurityManager(new ServiceSecurityManager());
    }

    static {
        instance = new ServiceManager(); //初始化实例
        instance.setLoader(new ContextClassLoader(Logger.getLog()));
    }

    @Override
    public Service getService(String serviceName) {
        if (serviceName != null && services.containsKey(serviceName)) {
            return services.get(serviceName);
        }
        return null;
    }

    public static ServiceManager getInstance() {
        return instance;
    }

    public void setLoader(ContextClassLoader loader) {
        classLoader = loader;
    }

    public ContextClassLoader getLoader() {
        return classLoader;
    }

    public void regisiter(Service service) {
        if (service == null) {
            throw new IllegalArgumentException("service is required");
        } else if (instance.services.containsKey(service.getServiceName())) {
            throw new IllegalArgumentException("service name was already regsition.name:" + service.getServiceName());
        }
        instance.services.put(service.getServiceName(), service);
    }
}
