/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util;

/**
 * 表示一个对象池。
 * @author sixmoon
 */
public interface Pool<T extends Object> {

    /**
     * 从池中获取一个对象，如果池为空则返回{@code null}。
     */
    T get();

    /**
     * 将一个对象放入池中。
     * @param item
     * @return 成功返回 {@code true}，否则返回 {@code false}。
     */
    boolean put(T item);

    /**
     * 销毁这个池。
     */
    void destory();
}
