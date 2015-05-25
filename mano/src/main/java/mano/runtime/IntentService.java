/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.runtime;

/**
 * 提供一个可处理 {@link Intent} 的服务抽象类。
 *
 * @author jun
 */
public abstract class IntentService extends AbstractService implements IntentContainer {

    protected class IntentHandleImpl implements IntentHandle {

        Intent intent;
        //boolean done;
        volatile boolean running;
        volatile boolean cancelled;
        volatile boolean isErr;
        Throwable err;
        final java.util.concurrent.atomic.AtomicBoolean doneFloag = new java.util.concurrent.atomic.AtomicBoolean(false);

        @Override
        public void notifyDone() {
            synchronized (doneFloag) {
                doneFloag.set(true);
                doneFloag.notifyAll();
            }
        }

        @Override
        public void setError(Throwable ex) {
            synchronized (doneFloag) {
                doneFloag.set(true);
                isErr = true;
                err = ex;
                doneFloag.notifyAll();
            }
        }

        @Override
        public void cancel() {
            synchronized (doneFloag) {
                if (!running && !doneFloag.get()) {
                    cancelled = true;
                    doneFloag.set(true);
                    //((ArrayBlockingQueue) queued).remove(this);
                    doneFloag.notifyAll();
                    if (intent.getCallback() != null) {
                        try {
                            intent.getCallback().run(intent);
                        } catch (Throwable ex) {
                            //忽略
                        }
                    }
                }
            }
        }

        @Override
        public boolean isDone() {
            return doneFloag.get();
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public boolean isError() {
            return isErr;
        }

        @Override
        public Throwable getError() {
            return err;
        }

        @Override
        public Intent await() throws InterruptedException {
            return this.await(0);
        }

        @Override
        public Intent await(int timeout) throws InterruptedException {
            if (!doneFloag.get()) {
                synchronized (doneFloag) {
                    //System.out.println("wait:" + doneFloag.get());
                    if (timeout > 0) {
                        doneFloag.wait(timeout);
                    } else {
                        doneFloag.wait();
                    }
                }
            }
            return intent;
        }

        @Override
        public IntentService getService() {
            return IntentService.this;
        }

        @Override
        public Intent getIntent() {
            return intent;
        }

    }

    //protected final Queue<IntentHandleImpl> queued = new ArrayBlockingQueue<>();

    /**
     * 执行 {@code intent}。
     *
     * @param intent {@link Intent} 的实例。
     * @throws java.lang.Exception 执行过程中异常。
     */
    public abstract void execute(Intent intent) throws Exception;

    @Override
    protected void onStart() throws Exception {
        doStartBefore();

        if (scheduler() == null) {
            throw new NullPointerException("scheduler()");
        }

//        IntentHandleImpl handle;
//        while (this.isRunning()) {
//            //System.out.println("LOOP:" + DateTime.now());
//            if (queued.isEmpty()) {
//                synchronized (queued) {
//                    queued.wait();//TODO:超时？
//                }
//                //System.out.println("LOOP:" + DateTime.now());
//                //Thread.sleep(1000);
//                continue;
//            }
//            handle = queued.poll();
//            if (handle != null) {
//                try {
//                    //System.out.println(">>>>"+handle.intent);
//                    execute(handle.intent);
//                    handle.notifyDone();
//                } catch (Throwable ex) {
//                    if(Log.TRACE.isDebugEnabled()){
//                        Log.TRACE.debug(ex);
//                    }
//                    handle.setError(ex);
//                } finally {
//                    try {
//                        handle.intent.getCallback().run(handle.intent);
//                    } catch (Throwable ex) {
//                        //忽略
//                    }
//                }
//
//            }
//        }
    }

    /**
     * 获取用于调度程序。
     *
     * @return
     */
    protected IntentServiceScheduler scheduler() {
        return Bootstrap.getIntentServiceScheduler();
    }

    @Override
    protected void onStop() throws Exception {
        doStopBefore();
//        IntentHandleImpl handle;
//        synchronized (queued) {
//            while ((handle = queued.poll()) != null) {
//                handle.cancel();
//            }
//        }
    }

    @Override
    public IntentHandle queueIntent(Intent intent) {
        if (!this.isRunning()) {
            throw new IllegalStateException("This service has been stopped or not running.");
        } else if (intent == null) {
            throw new NullPointerException("intent");
        }
        IntentHandleImpl handle = new IntentHandleImpl();
        handle.intent = intent;

        if (scheduler() == null) {
            throw new NullPointerException("scheduler()");
        } else if (scheduler().submit(handle)) {
            return handle;
        }
        return null;

//        if (queued.offer(handle)) {
//            synchronized (queued) {
//                queued.notifyAll();
//            }
//            return handle;
//        }
//        return null;
    }

    @Override
    public boolean hasIntents() {
        if (scheduler() == null) {
            throw new NullPointerException("scheduler()");
        }
        return scheduler().getPaddingSize() != 0;
    }

    /**
     * @deprecated 
     * @throws Exception 
     */
    protected void doStartBefore() throws Exception{}

    /**
     * @deprecated 
     * @throws Exception 
     */
    protected void doStopBefore() throws Exception{}

}
