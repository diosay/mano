/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.runtime;

/**
 *
 * @author jun
 */
public interface IntentHandle {
    void notifyDone();
    void setError(Throwable ex);
    IntentService getService();
    Intent getIntent();
    void cancel();
    Intent await() throws InterruptedException;
    Intent await(int timeout) throws InterruptedException;
    Throwable getError();
    boolean isCancelled();
    boolean isDone();
    boolean isError();
}
