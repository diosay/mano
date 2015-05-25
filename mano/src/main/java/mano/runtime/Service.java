/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.runtime;

/**
 * 表示一个运行时服务。
 * @author jun
 */
public interface Service extends Runnable {
    
    
     /**
     * 用于获取启动服务时的配置文件的{@code key}。
     */
    public static final String PROP_CONFIG_FILE="service.config.file";
    public static final String PROP_CLASS_LOADER="service.classloader";
    public static final String PROP_LOGGER="service.logger";
    public static final String PROP_NAME="service.name";
    /**
     * 获取当前服务的唯一名称。
     * <p>注意：该名称在{@link ServiceContainer} 中只能有一个。
     * @return 服务名称。
     */
    String getServiceName();
    
    /**
     * 获取当前服务的容器。
     * @return {@link ServiceContainer}
     */
    ServiceContainer getContainer();
    
    /**
     * 在初始化之前设置的必要的运行时属性。
     * @param key 属性名称。
     * @param value 属性值。
     * @return 当前对象的实例。
     */
    Service setProperty(String key, Object value);
    
    /**
     * 根据键名获取属性值。
     * @param key 属性名称。
     * @param def 如果未找到时返回的默认值。
     * @return 找到返回对应属性的值，否则返回 {@code def} 给定的值。
     */
    Object getProperty(String key, Object def);
    
    /**
     * 执行服务。该方法可多次调用，但只有成功的执行一次。
     */
    @Override
    void run();
    
    /**
     * 停止服务。该方法可多次调用，但只有成功的执行一次。
     */
    void stop();
    
    /**
     * 获取一个值，以指示当前服务的运行状态。
     * @return {@code true} 表示正在运行,否则未运行。
     */
    boolean isRunning();
    
    /**
     * 当初始化时调用。
     * @throws Exception 
     */
    //void onInit() throws Exception;
    
    /**
     * 当初始化完成后调用。
     * @throws Exception 
     */
    //void onStart() throws Exception;
    
    /**
     * 当停止服务时运行。
     * @throws Exception 
     */
    //void onStop() throws Exception;
    
}
