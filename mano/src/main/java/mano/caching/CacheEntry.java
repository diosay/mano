/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.caching;

/**
 * 表示一个缓存项。
 * @author jun <jun@diosay.com>
 */
public interface CacheEntry {

    /**
     * 获取缓存的键。
     * @return 
     */
    String getKey();

    /**
     * 获取缓存的值。
     * @return 
     */
    Object getValue();
    
    /**
     * 获取最后访问的时间。
     * @return 
     */
    long getLastVisited();
    
    /**
     * 获取超时的值。
     * @return 
     */
    long getTimeout();
    
    /**
     * 是否能更新。
     * @return 
     */
    boolean canUpdate();
    
    /**
     * 该缓存项是否过期。
     * @return 
     */
    boolean isExpired();
}
