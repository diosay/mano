/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano;

/**
 * 封装一个方法，该方法只有一个参数并且不返回值。
 * @author jun <jun@diosay.com>
 */
@FunctionalInterface
public interface Action<T> {
    /**
     * 封装的方法。
     * @param arg 
     */
    void run(T arg);
}
