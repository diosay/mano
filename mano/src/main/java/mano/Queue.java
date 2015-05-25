/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano;

/**
 * 提供一个比{@link java.util.Queue} 更简结的队列接口。
 * @author sixmoon
 */
public interface Queue<E> {
    
    /**
     * 返回当前集合的队列总数量。
     */
    int size();
    
    /**
     * 判断当前队列是否为空。
     * @return 
     */
    boolean isEmpty();
    
    /**
     * 将一个元素添加到队列尾部。
     * @param e 元素
     * @return 成功返回{@code true}，否则返回{@code false}。
     */
    boolean offer(E e);
    
    /**
     * 获取并移除队首的元素，如果队列为空则返回 {@code null}。 
     */
    E poll();
    
    /**
     * 获取队首的元素(不移除)，如果队列为空则返回 {@code null}。 
     */
    E peek();
    
    /**
     * 循环并移除，action返回为 true 的对象。
     * @param action 
     */
    void forEachRemove(Callback<? super E,Boolean> action);
}
