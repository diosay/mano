/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.service;

import mano.ContextClassLoader;

/**
 * 
 * @author johnwhang
 */
public abstract class Service implements Runnable {

    private ContextClassLoader loader;
    private ServiceContainer container;
    private boolean isRunning;
    private boolean isStopped;
    /**
     * 用于获取启动服务时的配置文件的{@code key}。
     */
    public static final String SERVICE_CONFIG_KEY="service.config.file";
    public final synchronized void init(ContextClassLoader loader, ServiceContainer container) {
        if (loader == null) {
            throw new NullPointerException("loader");
        }
        if (container == null) {
            throw new NullPointerException("container");
        }
        container.add(this);
        this.loader = loader;
        this.container = container;
    }

    @Override
    public final synchronized void run() {
        if (isRunning || this.isStopped) {
            return;
        }
        try {
            this.isRunning = true;
            this.onInit();
            if (loader.getLogger().isDebugEnabled()) {
                loader.getLogger().debug("Service is initialized:" + this.getServiceName());
            }
            this.onStart();
        } catch (Exception ex) {
            if (loader.getLogger().isErrorEnabled()) {
                loader.getLogger().error("Service was initializing failed:"+ this.getServiceName(),ex);
            }
            stop();
        }
    }

    public final synchronized void stop() {
        if (!this.isRunning || this.isStopped) {
            return;
        }
        try {
            this.onStop();
        } catch (Exception ex) {
            if (loader.getLogger().isErrorEnabled()) {
                loader.getLogger().error(ex);
            }
        }
        this.isRunning = false;
        this.isStopped = true;
        this.container.remove(this);
        if (loader.getLogger().isDebugEnabled()) {
            loader.getLogger().debug("Service is stopped:" + this.getServiceName());
        }
        try {
            this.onDestroy();
        } catch (Exception ex) {
            if (loader.getLogger().isDebugEnabled()) {
                loader.getLogger().debug(ex);
            }
        }
    }

    public final ContextClassLoader getContext() {
        return this.loader;
    }
    
    public final boolean isRunning(){
        return this.isRunning;
    }

    public abstract String getServiceName();

    protected abstract void onInit() throws Exception;

    protected abstract void onStart() throws Exception;

    protected abstract void onStop() throws Exception;

    protected void onDestroy() throws Exception {
        this.loader = null;
        this.container = null;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            this.stop();
        } catch (Throwable e) {
            //nop
        }
        super.finalize();
    }
}
