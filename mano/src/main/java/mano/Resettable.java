/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano;

/**
 * 定义一种可重置当前对象状态的方法。
 * @author jun <jun@diosay.com>
 */
public interface Resettable {
    
    /**
     * 重置对象到对象初始状态.
     */
    void reset();
}
