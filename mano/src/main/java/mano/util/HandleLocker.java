/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author sixmoon
 */
public class HandleLocker implements HandleUnlocker {

    private final Semaphore lock = new Semaphore(1);

    public boolean lock() {
        try {
            lock.acquire();
            return true;
        } catch (InterruptedException ex) {
            return false;
        }
    }
    
    public boolean lock(long timeout) {
        try {
            return lock.tryAcquire(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            return false;
        }
    }
    

    @Override
    public void unlock() {
        lock.release();
    }

}
