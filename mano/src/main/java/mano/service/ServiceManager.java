/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import mano.ContextClassLoader;
import mano.InvalidOperationException;
import mano.util.logging.Logger;

/**
 * 全局的服务管理器。logger.get(name).err(msg,err)
 *logger.add(handler)
 * logger.remove(handler)
 * @author junhwong
 */
public abstract class ServiceManager implements ServiceContainerBase{

    @Override
    public AbstractService getService(String serviceName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private final Map<String, AbstractService> services;
    private ContextClassLoader classLoader;
    private static ServiceManager instance;

    /**
     * 初始化 ServiceManager 的单例。
     * @param loader 
     */
    protected ServiceManager(ContextClassLoader loader) {
        if(loader==null){
            throw new IllegalArgumentException("loader");
        }
        if(instance==null){
            instance=this;
        }else{
            throw new InvalidOperationException("服务管理器已经初始化。");
        }
        classLoader=loader;
        services = new ConcurrentHashMap<>();
    }

    
    public static AbstractService get(String serviceName) {
        if (serviceName != null && getInstance().services.containsKey(serviceName)) {
            return getInstance().services.get(serviceName);
        }
        return null;
    }

    /**
     * 获取当前管理器的实例。
     * @return 
     */
    static ServiceManager getInstance() {
        return instance;
    }

    /**
     * 获取类加载器。
     * @return 
     */
    public static ContextClassLoader getLoader() {
        return getInstance().classLoader;
    }
    
    /**
     * 获取日志器。
     * @return 
     */
//    public static Logger getLogger(){
//        return getInstance().classLoader.getLogger();
//    }

    /**
     * 注册一个服务。
     * @param service 
     */
    public void regisiter(AbstractService service) {
        if (service == null) {
            throw new IllegalArgumentException("service is required");
        } else if (instance.services.containsKey(service.getServiceName())) {
            throw new IllegalArgumentException("service name was already regsition.name:" + service.getServiceName());
        }
        instance.services.put(service.getServiceName(), service);
    }
}
