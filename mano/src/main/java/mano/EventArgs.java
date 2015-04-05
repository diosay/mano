/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano;

/**
 * 表示包含事件数据的类的基类。
 *
 * @author sixmoon
 */
public interface EventArgs {

    /**
     * 提供一个空的事件参数。
     */
    public static final EventArgs Empty = new EventArgs() {
    };

}
