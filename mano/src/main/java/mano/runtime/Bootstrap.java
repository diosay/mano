/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.runtime;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mano.Queue;
import mano.logging.Log;
import mano.logging.LogService;
import mano.util.ArrayBlockingQueue;
import mano.util.ScheduleTask;
import mano.util.ThreadPool;

/**
 *
 * @author jun
 */
public abstract class Bootstrap {

    private static IntentServiceScheduler intentServiceScheduler;

    public static final IntentServiceScheduler getIntentServiceScheduler() {
        return intentServiceScheduler;
    }

    private class DefaultIntentServiceScheduler implements IntentServiceScheduler {

        final Queue<IntentHandle> queued = new ArrayBlockingQueue<>();

        @Override
        public int getPaddingSize() {
            return queued.size();
        }

        @Override
        public boolean submit(IntentHandle handle) {
            if (queued.offer(handle)) {
                synchronized (queued) {
                    queued.notifyAll();
                }
                return true;
            }
            return false;

        }

        void workerRun() throws Exception {
            String name = "Default IntentServiceScheduler Worker @ " + Thread.currentThread().getId() + "/" + this.hashCode();
            Thread.currentThread().setName(name);
            if (Log.TRACE.isDebugEnabled()) {
                Log.TRACE.debug(name + "  >> running");
            }
            IntentHandle handle = null;
            while (true) {
                if (queued.isEmpty()) {
                    synchronized (queued) {
                        queued.wait(5000);//wait 5s
                    }
                }
                handle = queued.poll();
                if (handle != null) {
                    try {
                        //System.out.println(">>>>"+handle.intent);
                        if (handle.getService() != null && handle.getService().isRunning()) {
                            handle.getService().execute(handle.getIntent());
                            handle.notifyDone();
                        } else {
                            handle.setError(new IllegalMonitorStateException("IntentService not running or stopped."));
                        }
                    } catch (Throwable ex) {
                        if (Log.TRACE.isDebugEnabled()) {
                            Log.TRACE.debug(ex);
                        }
                        handle.setError(ex);
                    } finally {
                        if (handle.getIntent().getCallback() != null) {
                            try {
                                handle.getIntent().getCallback().run(handle.getIntent());
                            } catch (Throwable ex) {
                                //忽略
                            }
                        }
                    }
                } else {
                    Thread.yield();//
                }
            }
        }

        DefaultIntentServiceScheduler run() {
            for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {

                mano.util.ThreadPool.execute(() -> {
                    String tname = Thread.currentThread().getName();
                    try {
                        workerRun();
                    } catch (Exception ex) {
                        exceptionCaught(ex);
                    }
                    if (Log.TRACE.isDebugEnabled()) {
                        Log.TRACE.debug(Thread.currentThread().getName() + "  >> stopped");
                    }
                    Thread.currentThread().setName(tname);
                });
            }
            return this;
        }
    }

    protected IntentServiceScheduler createIntentServiceScheduler() {
        return new DefaultIntentServiceScheduler().run();
    }
    long scheduler_period;
    private void runTimedScheduleTask() throws InterruptedException{
        long current;
        Iterator<ScheduleTask> iter;
        ScheduleTask task;
        while (true) {

            Thread.sleep(200);
            current = System.currentTimeMillis();
            iter = ScheduleTask.scheduledTasks.iterator();
            while (iter.hasNext()) {
                try {
                    task = iter.next();
                    if (task.execute(current)) {
                        ScheduleTask.scheduledTasks.remove(task);
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
    }
    

    /**
     * 异常处理。
     *
     * @param cause
     */
    protected void exceptionCaught(Throwable cause) {
        cause.printStackTrace(System.out);
    }

    protected abstract void doStart(String[] args) throws Exception;

    protected abstract void doStop() throws Exception;

    public final void start(String[] args) {

        intentServiceScheduler = createIntentServiceScheduler();

        try {
            doStart(args);
            Thread.currentThread().setName("Mano boostrap & Scheduler");
            runTimedScheduleTask();
        } catch (Exception ex) {
            exceptionCaught(ex);
        }
        
        stop();
    }

    public final void stop() {
        intentServiceScheduler=null;
        try {
            doStop();
        } catch (Exception ex) {
            exceptionCaught(ex);
        }
    }

}
