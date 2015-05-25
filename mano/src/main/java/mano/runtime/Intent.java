/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.runtime;

import java.util.HashMap;
import java.util.Map;
import mano.Action;

/**
 * 提供解决运行时服务之间通信的组件。
 * @author jun
 */
public final class Intent {
    private String serviceName;
    private String action;
    private volatile transient Action callback;
    private volatile transient IntentHandle handle;
    private final transient Map<String, Object> extras;
    public Intent(String serviceName,String action){
        if(serviceName==null || "".equals(serviceName)){
            throw new java.lang.NullPointerException("serviceName");
        }
        if(action==null || "".equals(action)){
            throw new java.lang.NullPointerException("action");
        }
        this.serviceName=serviceName;
        this.action=action;
        this.extras = new HashMap<>();
    }
    
    /**
     * 设置用于通信双方的参数。
     * @param key
     * @param value 
     */
    public final void set(String key, Object value) {
        extras.put(key, value);
    }

    /**
     * 获取用于通信双方的参数。
     * @param key
     * @return 
     */
    public final Object get(String key) {
        return extras.get(key);
    }

    /**
     * 获取扩展参数的键集合。
     * @return 
     */
    public final Iterable<String> getKeys() {
        return extras.keySet();
    }
    
    /**
     * 获取目标服务的名称。
     * @return 
     */
    public final String getServiceName() {
        return serviceName;
    }

    /**
     * 获取目标服务调用的action。
     * @return 
     */
    public final String getAction() {
        return action;
    }
    
    /**
     * 获取在执行过程中的异常。
     * @return 
     */
    public Throwable getException(){
        if(handle==null){
            throw new IllegalArgumentException("This Intent not submit yet.");
        }
        return handle.getError();
    }
    
    public Action<Intent> getCallback(){
        return this.callback;
    }
    
    public synchronized final Intent submit() {
        return this.submit(null);
    }
    
    /**
     * 设置一个回调，并提交到服务总线。
     * @param callback 执行完成后的回调方法。
     * @return 
     */
    public synchronized final Intent submit(Action<Intent> callback) {
        if(handle!=null){
            throw new IllegalArgumentException("This Intent already has been submitted.");
        }
        this.callback=callback;
        Service service=ServiceContainer.getService(serviceName);
        if(service!=null && service instanceof IntentContainer){
            handle=((IntentContainer)service).queueIntent(this);
            if(handle==null){
                throw new IllegalArgumentException("Failed to submitting.");
            }else{
                //System.out.println("reg to svc:"+service.getServiceName());
            }
        }else{
            throw new IllegalArgumentException("Give serviceName["+serviceName+"] not found or not a IntentContainer.");
        }
        return this;
    }
    
    public boolean cancel(){
        if(handle==null){
            throw new IllegalArgumentException("This Intent not submit yet.");
        }
        handle.cancel();
        return handle.isCancelled();
    }
    
    public boolean isCancelled(){
        return handle==null?false:handle.isCancelled();
    }
    
    public boolean isDone(){
        return handle==null?false:handle.isDone();
    }
    
    /**
     * 获取 Intent 是否由于处理异常的原因而完成。
     *
     * @return
     */
    public boolean isFaulted() {
        return handle==null?false:handle.isError();
    }
    
    public Intent await() throws InterruptedException{
        if(handle==null){
            throw new IllegalArgumentException("This Intent not submit yet.");
        }
        handle.await();
        return this;
    }
    
    public Intent await(int timeout) throws InterruptedException{
        if(handle==null){
            throw new IllegalArgumentException("This Intent not submit yet.");
        }
        handle.await(timeout);
        return this;
    }
    
    
    
}
