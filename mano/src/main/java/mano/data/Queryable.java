/*
 * Copyright (C) 2014 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.data;

/**
 * 提供对特定数据源的查询功能。
 * @author junhwong
 */
public interface Queryable {
    /**
     * 获取别名。
     * @return 
     */
    String getAlias();
    /**
     * 获取关联的类型。
     * @return 
     */
    Class<?> getType();
    
    void join();
    void where();
    void order();
    void group();
}
