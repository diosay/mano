/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class ThreadPool {

    private static final ExecutorService service = Executors.newCachedThreadPool();

    public static ExecutorService getService() {
        return service;
    }

    public static void execute(Runnable command) {
        service.execute(command);
    }

    public static <T> Future<T> submit(Callable<T> task) {
        return service.submit(task);
    }

    private static java.util.concurrent.CopyOnWriteArrayList<Runnable> scheduledTasks = new java.util.concurrent.CopyOnWriteArrayList<>();
    private static Runnable scheduler = () -> {
        while (true) {
            
            try {
                Thread.sleep(1000 * 60 * 1);
            } catch (InterruptedException ex) {
                execute(ThreadPool.scheduler);
                System.out.println("Scheduler will be re-start timing,This operation was caused by Thread Interrupted.");
                return;
            }
            System.out.println("Executing schedule Task.");
            scheduledTasks.forEach(task -> {
                try {
                    task.run();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            });
            System.gc();
            System.out.println("Executed schedule Task.");
        }
    };

    static {
        execute(ThreadPool.scheduler);
    }

    /**
     * 添加调度任务。
     *
     * @param task
     */
    public static void addScheduledTask(Runnable task) {
        scheduledTasks.add(task);
    }

    /**
     * 移除调度任务。
     *
     * @param task
     */
    public static void removeScheduledTask(Runnable task) {
        scheduledTasks.remove(task);
    }

}
