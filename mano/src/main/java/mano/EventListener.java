/*
 * Copyright (C) 2014 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import mano.util.ThreadPool;

/**
 * 事件委托。
 * @author junhwong
 */
public class EventListener<S extends Object, A extends EventArgs> {
    
    private List<EventHandler> handlers = new ArrayList<>();
    
    /**
     * 添加处理程序
     * @param <H>
     * @param handler 
     */
    public <H extends EventHandler<S, A>> void addHandler(H handler) {
        handlers.add(handler);
    }
    
    /**
     * 移除处理程序。
     * @param <H>
     * @param handler 
     */
    public <H extends EventHandler<S, A>> void removeHandler(H handler) {
        handlers.remove(handler);
    }

    /**
     * 触发事件。
     *
     * @param sender 事件源。
     * @param args
     */
    public void fire(final S sender, final A args) {
        handlers.forEach(handler -> {
            handler.handle(sender, args);
        });
    }

    /**
     * 异步触发事件。
     *
     * @param sender 事件源。
     * @param args 参数对象。
     * @param executor 线程执行服务对象。如果为 null 则调用 {
     * @see mano.util.ThreadPool}
     */
    public void fire(final S sender, final A args, final ExecutorService executor) {
        handlers.forEach(handler -> {
            if(executor!=null){
                executor.submit(()->{
                    handler.handle(sender, args);
                });
            }else{
                ThreadPool.execute(()->{
                    handler.handle(sender, args);
                });
            }
        });
    }
}
