/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.io;


/**
 * 表示一个用于在 {@link Channel} 中排队的异步任务。
 * @author sixmoon
 */
@FunctionalInterface
public interface ChannelTask {
    /**
     * 当被成功调度时执行。
     * @param context {@link ChannelContext}
     */
    void execute(ChannelContext context);
}
