/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.data;

import java.sql.Connection;

/**
 * 用于维护数据一致性的上下文工作单元。
 * @author jun
 * @param <T> 连接/传话提供程序对象。
 */
public interface UnitOfWork<T> extends AutoCloseable{
    
    /**
     * @return the connection provider
     */
    T provider();
    
    /**
     * @return this Connection
     */
    Connection connect();
    
    /**
     * 提交本次更新。
     * @return 自己
     */
    UnitOfWork commit();
    
}
