/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.runtime;

import java.util.HashMap;
import java.util.Map;
import mano.EventArgs;
import mano.EventHandler;
import mano.EventListener;

/**
 * 运行时服务容器。
 * @author jun
 */
public class ServiceContainer {

    static final ServiceContainer instance;

    static {
        instance = new ServiceContainer();
    }

    /**
     * 从全局运行时服务容器中检索指定名称的的服务。
     * @param serviceName 要检索服务的服务名。
     * @return 找到返回服务，否则返回{@code null}。
     */
    public static Service getService(String serviceName) {
        if(serviceName==null || "".equals(serviceName)){
            throw new NullPointerException("serviceName");
        }
        return instance.get(serviceName);
    }

    public static EventListener<EventHandler<Integer, EventArgs>> sizeChanged() {
        return instance.sizeChangedEventHandle.getListener();
    }
    
    private final Map<String, Service> services;
    private final EventListener.EventListenerHandle<Integer, EventArgs> sizeChangedEventHandle;
    protected ServiceContainer() {
        services = new HashMap<>();
        sizeChangedEventHandle= EventListener.create();
    }

    /**
     * 获取服务。
     * @param name
     * @return 
     */
    protected synchronized Service get(String name) {
        return services.containsKey(name) ? services.get(name) : null;
    }

    /**
     * 将一个服务添加到当前容器中。
     *
     * @param service 要添加的服务实例。
     * @return {@code true}成功添加,否则{@code false}。
     */
    public synchronized boolean add(Service service) {
        if(service !=null && !services.containsKey(service.getServiceName())){
            services.put(service.getServiceName(), service);
            sizeChangedEventHandle.fire(services.size(), EventArgs.Empty);
            return true;
        }
        return false;
    }

    /**
     * 将一个服务从当前容器中移除。
     *
     * @param service 要移除的服务实例。
     * @return {@code true}成功移除,否则{@code false}。
     */
    public synchronized boolean remove(Service service) {
        
        if(service !=null && services.containsKey(service.getServiceName())){
            services.remove(service.getServiceName());
            sizeChangedEventHandle.fire(services.size(), EventArgs.Empty);
            return true;
        }
        return false;
    }

    
    
}
