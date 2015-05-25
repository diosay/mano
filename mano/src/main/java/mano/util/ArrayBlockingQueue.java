/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;
import mano.Callback;
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
        boolean result;
        lock.lock();
        try {
            result= super.add(e);
        } finally {
            lock.unlock();
        }
        
        if(result){//TODO:关注该代码的效率
            synchronized(this){
                this.notifyAll();
            }
        }
        return result;
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
    
    
    private ArrayList<Integer> removes =new ArrayList<>();
    @Override
    public void forEachRemove(Callback<? super E,Boolean> action){
        lock.lock();
        try {
            removes.clear();
            for(int i=0;i<size();i++){
                if(action.call(get(i))){
                    removes.add(i);
                }
            }
            for(int i=0;i<removes.size();i++){
                remove(i);
            }
            removes.clear();
        } finally {
            lock.unlock();
        }
    }
}
