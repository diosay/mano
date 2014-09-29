/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano;

/**
 * 表示一个对象的 Wrapper
 *
 * @author jun <jun@diosay.com>
 */
public class Value<T> {

    public T value;

    public Value() {
    }

    public Value(T value) {
        this.value = value;
    }

    /**
     * 获取值
     *
     * @return
     */
    public T get() {
        return value;
    }

    /**
     * 设置值。
     *
     * @param value
     */
    public void set(T value) {
        this.value = value;
    }

    /**
     * 是否是空值。
     * @return 
     */
    public boolean isNull() {
        return value == null;
    }

    public static <T> Value<T> wrap(T value) {
        return new Value<>(value);
    }
}
