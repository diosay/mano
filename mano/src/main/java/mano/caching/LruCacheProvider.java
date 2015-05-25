/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.caching;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 实现LRU算法的本地缓存提供程序。
 *
 * @author jun <jun@diosay.com>
 * @param <V>
 */
public class LruCacheProvider<V extends Object> extends HashCacheProvider<V> {

    /**
     * 获取或设置缓存大小。
     */
    private final int cacheSize;

    public LruCacheProvider(final int cacheSize) {
        //int hashTableCapacity = (int) Math.ceil (cacheSize / 0.75f) + 1;
        super(new LinkedHashMap<String, ItemEntry>((int)Math.ceil (cacheSize / 0.75F) + 1, 0.75F, true) {
            // (an anonymous inner class)  
            private static final long serialVersionUID = 1;

            @Override
            protected boolean removeEldestEntry(Map.Entry<String, ItemEntry> eldest) {
                return this.size() > cacheSize;
            }
        });
        this.cacheSize=cacheSize;
    }
}
