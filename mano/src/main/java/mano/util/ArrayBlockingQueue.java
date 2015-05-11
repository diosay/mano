/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;
import mano.Queue;

/**
 * 基础 {@link java.util.ArrayList}的数组队列。
 * <p>当前类实现除了接口 {@link Queue}以外的方法均不是线程安全的。
 * @author sixmoon
 */
public class ArrayBlockingQueue<E> extends ArrayList<E> implements Queue<E> {

    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public boolean offer(E e) {
        if (e == null) {
            return false;
        }
        lock.lock();
        try {
            return super.add(e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public E poll() {
        lock.lock();
        try {
            if (this.isEmpty()) {
                return null;
            }
            return this.remove(0);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public E peek() {
        lock.lock();
        try {
            if (this.isEmpty()) {
                return null;
            }
            return this.get(0);
        } finally {
            lock.unlock();
        }
    }

}
