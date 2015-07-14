/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.data;

/**
 * 用于维护数据一致性的上下文工作单元。
 * @author jun
 * @param <T> 连接传话对象。
 */
public interface UnitOfWork<T> extends AutoCloseable{
    
    /**
     * @return the session
     */
    T getSession();
    
    /**
     * 提交本次更新。
     * @return 自己
     */
    UnitOfWork commit();
    
}
