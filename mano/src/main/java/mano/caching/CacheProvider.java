/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.caching;

import mano.Action;
import mano.InvalidOperationException;

/**
 * 表示一个缓存提供程序。
 *
 * @author jun <jun@diosay.com>
 * @param <V>
 */
public interface CacheProvider<V extends Object> {

    /**
     * 添加或更新一个缓冲项。
     * @param key 缓存的检索键。
     * @param value 缓存对象。如果是一个 <code>Map<K,V></code>对象，则可以使用<code>get(String key,String index)</code>方法获取具体索引的值。
     * @param timeout 相对于最后访问的过期时间(毫秒)。
     * @param update 用于控制缓冲的生命周期，如果为真每次成功获取缓冲都将自动更新最后访问时间，否则最后访问时间将只会是创建时间。
     * @param callback 在移除时调用，如果已经设置。
     * @throws InvalidOperationException 如果对象不能被缓存则抛出该异常。
     */
    void set(String key, V value, long timeout, boolean update,Action<CacheEntry> callback) throws InvalidOperationException;

    boolean set(String key,String index, Object value);
    
    boolean contains(String key);
    /**
     * 根据 key 获取缓存项，失败返回 null。
     * @param key 缓存的检索键。
     * @return 如果对象不存在或过期时间已到，返回该缓存项。否则返回 null。
     */
    V get(String key);
    
    /**
     * 根据 key 获取缓存项，失败返回 null。
     * @param key 缓存的检索键。
     * @return 如果对象不存在或过期时间已到，返回该缓存项。否则返回 null。
     */
    CacheEntry<V> getEntry(String key);
    
    /**
     * 根据 key 获取字典缓存项的指定索引值，失败返回 null。
     * @param key 缓存的检索键。
     * @param index 索引。
     * @return 如果对象不存在或过期时间已到，返回该缓存项。否则返回 null。
     */
    Object get(String key,String index);

    /**
     * 移除一个缓存项。
     * @param key 缓存的检索键。
     * @return 
     */
    CacheEntry<V> remove(String key);
    
    /**
     * 移除一个缓存项。
     * @param key 缓存的检索键。
     * @param index
     */
    void remove(String key,String index);

    /**
     * 刷新所有缓存项，并保存状态（如果可用）。
     */
    void flush();
}
