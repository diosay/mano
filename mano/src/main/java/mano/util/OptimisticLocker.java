/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 乐观锁。
 *
 * @author jun <jun@diosay.com>
 */
public final class OptimisticLocker {

    private final AtomicBoolean pendding = new AtomicBoolean(false);
    private volatile LockStateImpl ref = new LockStateImpl();

    public LockStateImpl acquire(int timeout) {
        synchronized (pendding) {
            if (pendding.get()) {
                try {
                    pendding.wait(timeout);
                } catch (InterruptedException ex) {
                    return null;
                }
            }
            if (pendding.get()) {
                return null;
            } else {
                ref.state.set(false);
                pendding.set(true);
            }
            return ref;
        }
    }

    public void release(LockState state) throws IllegalMonitorStateException {
        if (state == null || !pendding.get() || !state.equals(ref)) {
            throw new IllegalMonitorStateException("state");
        }
        synchronized (ref.state) {
            ref.state.set(true);
            ref.state.notify();
        }
        synchronized (pendding) {
            pendding.set(false);
            pendding.notify();//
        }
    }

    private class LockStateImpl implements LockState {

        private final AtomicBoolean state = new AtomicBoolean(false);

        public void notifyDone(){
            release(this);
        }
        
        @Override
        public boolean isDone() {
            return state.get();
        }

        @Override
        public void waitDone() throws InterruptedException {
            synchronized (state) {
                if (!state.get()) {
                    state.wait();
                }
            }
        }
    }
}
