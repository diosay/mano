/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 支持自动回收的对象缓存池。
 *
 * @author sixmoon
 */
public abstract class CachedObjectRecyler<T> implements Pool<T> {

    private final ReentrantLock lock = new ReentrantLock();
    private final ArrayList<T> elements = new ArrayList<>();
    private boolean closed;
    private long last;
    private int timeout = 5000;

    public CachedObjectRecyler() {
        ScheduleTask.register(current -> {

            if (current - last >= timeout) {
                last = current;
                lock.lock();
                try {
                    onRecyle(elements);
                } finally {
                    lock.unlock();
                }
            }
            return closed;
        });
    }

    /**
     * 创建一个新对象。
     *
     * @return
     */
    protected abstract T createNew();

    protected void onRecyle(List<T> list) {
        int size = list.size();
        size = size / 2 + (size % 2 == 0 ? 0 : 1);
        while (size > 0) {
            list.remove(0);
            size--;
        }
        //System.out.println("Remaining Cached Items: " + items.size());
    }

    @Override
    public T get() {
        if (closed) {
            throw new IllegalStateException("Current recyler was already closed.");
        }
        T item = null;
        lock.lock();
        try {
            if (!elements.isEmpty()) {
                item = elements.get(0);
                elements.remove(0);
            }
            if (item == null) {
                item = createNew();
            }
        } finally {
            lock.unlock();
        }
        return item;
    }

    @Override
    public boolean put(T item) {
        if (closed) {
            throw new IllegalStateException("Current recyler was already destoryed.");
        }
        if (item == null) {
            return false;
        }
        lock.lock();
        try {
            if (!elements.isEmpty()) {
                item = elements.get(0);
                elements.remove(0);
            }
            if (elements.contains(item)) {
                return false;
            } else {
                return elements.add(item);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void destory() {
        if (!closed) {
            lock.lock();
            try {
                elements.clear();
            } finally {
                lock.unlock();
                closed = true;
            }
        }
    }
}
