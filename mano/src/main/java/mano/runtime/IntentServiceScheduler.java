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
public interface IntentServiceScheduler {
    /**
     * 获取待处理的队列。
     * @return 
     */
    int getPaddingSize();
    
    /**
     * 提交一个 {@link IntentHandle} 到队列中。
     * @param handle
     * @return 
     */
    boolean submit(IntentHandle handle);
    
}
