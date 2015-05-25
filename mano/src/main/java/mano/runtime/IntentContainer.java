/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.runtime;

/**
 * 提供可供 {@link Intent} 排队并执行的容器。
 * @author jun
 */
public interface IntentContainer {
    /**
     * 将 {@code Intent} 加入执行队列。
     * @param intent {@link Intent} 的实例。
     * @return {@link IntentHandle}实例。
     */
    IntentHandle queueIntent(Intent intent);
    
    /**
     * 判断队列中是否还有未处理的任务。
     * @return {@code true}表示还有，否则 {@code false}。
     */
    boolean hasIntents();
}
