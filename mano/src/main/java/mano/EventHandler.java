/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano;

/**
 * 表示一个事件处理程序。
 * @author sixmoon
 */
@FunctionalInterface
public interface EventHandler<S extends Object,A extends EventArgs> {
    /**
     * 处理事件。
     * @param sender 事件源对象实例。
     * @param args 事件参数。
     */
    void handle(S sender,A args);
}
