/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.runtime;

import java.util.HashMap;
import java.util.Map;

/**
 * 实现了部分功能的运行时服务 {@link Service} 抽象类。
 * @author jun
 */
public abstract class AbstractService implements Service {

    private ServiceContainer container;
    private boolean isRunning;
    private boolean isStopped;
    private String serviceName;
    
    /**
     * 属性集合。
     */
    protected final Map<String, Object> props = new HashMap<>();

    /**
     * 异常处理。
     * @param cause 
     */
    protected void exceptionCaught(Throwable cause) {
        cause.printStackTrace();
    }

    @Override
    public String getServiceName() {
        return serviceName == null ? this.toString() : serviceName;
    }

    @Override
    public final ServiceContainer getContainer() {
        return container;
    }

    @Override
    public Service setProperty(String key, Object value) {
        if (key == null || "".equals(key)) {
            throw new NullPointerException("key");
        }
        if (PROP_NAME.equalsIgnoreCase(key) && value != null) {
            serviceName = value.toString();
        }
        props.put(key, value);
        return this;
    }

    @Override
    public Object getProperty(String key, Object def) {
        if (key == null || "".equals(key)) {
            throw new NullPointerException("key");
        }
        if (PROP_NAME.equalsIgnoreCase(key)) {
            return this.getServiceName();
        }
        return props.getOrDefault(key, def);
    }

    @Override
    public final void run() {
        if (isRunning && isStopped) {
            return;
        }
        try {
            onInit();
            isRunning = true;
            if (ServiceContainer.instance.add(this)) {
                container = ServiceContainer.instance;
            }
            onStart();
        } catch (Exception ex) {
            exceptionCaught(ex);
            stop();
        }
    }

    @Override
    public final void stop() {
        if (!isRunning && isStopped) {
            return;
        }
        try {
            isRunning = false;
            ServiceContainer.instance.remove(this);
            onStop();
        } catch (Exception ex) {
            exceptionCaught(ex);
        } finally {
            isStopped = true;
            props.clear();
        }
    }

    @Override
    public final boolean isRunning() {
        return isRunning && !isStopped;
    }

    /**
     * 当初始化时调用。
     * @throws Exception 
     */
    protected abstract void onInit() throws Exception;

    /**
     * 当初始化完成后调用。
     * @throws Exception 
     */
    protected abstract void onStart() throws Exception;

    /**
     * 当停止服务时运行。
     * @throws Exception 
     */
    protected abstract void onStop() throws Exception;

}
