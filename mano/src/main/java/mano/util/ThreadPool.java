/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util;

import java.util.Iterator;
import java.util.List;
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

    private static java.util.concurrent.CopyOnWriteArrayList<Runnable> scheduledTasksold = new java.util.concurrent.CopyOnWriteArrayList<>();
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
            scheduledTasksold.forEach(task -> {
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

    /**
     * 调度任务集合。
     */
    static final List<ScheduleTask> scheduledTasks = new java.util.concurrent.CopyOnWriteArrayList<>();
    static long scheduler_period;
    private static Runnable scheduler2 = () -> {
        long current;
        Iterator<ScheduleTask> iter;
        ScheduleTask task;
        while (true) {

            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                Thread thread=new Thread(ThreadPool.scheduler2);
                thread.setDaemon(true);
                thread.start();
                System.out.println("Scheduler will be re-start timing,This operation was caused by Thread Interrupted.");
                return;
            }
            current = System.currentTimeMillis();
            iter = scheduledTasks.iterator();
            while (iter.hasNext()) {
                try {
                    task = iter.next();
                    if (task.execute(current)) {
                        scheduledTasks.remove(task);
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            if (current - scheduler_period > 5000) {
                scheduler_period = current;
                System.gc();
            }
            //System.out.println("Executed schedule Task.");
        }
    };

    static {
        //execute(ThreadPool.scheduler);
        Thread thread=new Thread(ThreadPool.scheduler2);
        thread.setDaemon(true);
        thread.start();
        //execute(ThreadPool.scheduler2);
    }

    /**
     * 添加调度任务。
     *
     * @param task
     */
    public static void addScheduledTask(Runnable task) {
        scheduledTasksold.add(task);
    }

    /**
     * 移除调度任务。
     *
     * @param task
     */
    public static void removeScheduledTask(Runnable task) {
        scheduledTasksold.remove(task);
    }
}
