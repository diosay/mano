/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.web;

import java.util.Map;
import mano.caching.CacheProvider;
import mano.caching.HashCacheProvider;
import mano.service.AbstractService;
import mano.service.ServiceContainer;
import mano.service.ServiceProvider;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class HttpSessionService extends AbstractService implements ServiceProvider {

    HashCacheProvider provider;

    @Override
    public String getServiceName() {
        return "cache.service";
    }

    @Override
    public void run() {
        provider = new HashCacheProvider();
        this.onStart();
    }

    @Override
    public <T> T getService(Class<T> serviceType) {
        if (CacheProvider.class.getName().equals(serviceType.getName())) {
            return (T) provider;
        }
        return null;
    }

}
