/*
 * Copyright (C) 2014 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.data;

import java.io.Serializable;

/**
 * 表示一条数据记录。
 * @author junhwong
 */
public interface Record extends Serializable{
    /**
     * 获取值。
     * @param name
     * @return 
     */
    Serializable get(String name);
    
    /**
     * 设置值。
     * @param name
     * @param value
     * @return 
     */
    Record set(String name,Serializable value);
}
