/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import mano.util.ThreadPool;

/**
 * 事件委托。
 *
 * @author sixmoon
 */
public class EventListener<H extends EventHandler> {

    private final List<H> handlers = new CopyOnWriteArrayList<>();

    private EventListener(){}
    
    /**
     * 添加处理程序
     *
     * @param <H>
     * @param handler
     */
    public void add(H handler) {
        handlers.add(handler);
    }

    /**
     * 移除处理程序。
     *
     * @param <H>
     * @param handler
     */
    public void remove(H handler) {
        handlers.remove(handler);
    }

    public static EventListenerHandle create() {
        return new EventListenerHandle(new EventListener());
    }

    public static class EventListenerHandle<S extends Object, A extends EventArgs> {

        private EventListener listener;

        private EventListenerHandle(EventListener listener) {
            this.listener = listener;
        }
        
        public EventListener getListener(){
            return listener;
        }

        /**
         * 触发事件。
         *
         * @param sender 事件源。
         * @param args
         */
        public void fire(final S sender, final A args) {

            listener.handlers.forEach(handler -> {
                ((EventHandler) handler).handle(sender, args);
            });
        }

//    /**
//     * 异步触发事件。
//     *
//     * @param sender 事件源。
//     * @param args 参数对象。
//     * @param executor 线程执行服务对象。如果为 null 则调用 {@link mano.util.ThreadPool}
//     */
//    public void fire(final S sender, final A args, final ExecutorService executor) {
//        handlers.forEach(handler -> {
//            if(executor!=null){
//                executor.submit(()->{
//                    handler.handle(sender, args);
//                });
//            }else{
//                ThreadPool.execute(()->{
//                    handler.handle(sender, args);
//                });
//            }
//        });
//    }
    }

}
