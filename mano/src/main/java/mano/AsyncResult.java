/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano;

/**
 * 表示异步操作的状态。
 * @author sixmoon
 */
public interface AsyncResult {
    /**
     * 获取一个值，该值指示异步操作是否已完成。
     */
    boolean isCompleted();
    
    /**
     * 阻止当前线程，直到该异步操作完成。
     * @param timeout 超时时间。单位：毫秒。
     * @return 成功完成返回 true，否则返回 false (包括超时和中断)。
     */
    boolean await(long timeout);
    
    /**
     * 将给定对象附加到当前对象中。
     * @param attachment 附件。
     */
    void attach(Object attachment);
    /**
     * 获取当前附加，如果没有则返回 null。
     */
    Object attachment();
}
