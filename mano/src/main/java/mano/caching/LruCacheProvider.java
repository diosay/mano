/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.caching;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.util.Callback;
import mano.InvalidOperationException;

/**
 * 实现LRU算法的本地缓存提供程序。
 *
 * @author jun <jun@diosay.com>
 */
public class LruCacheProvider implements CacheProvider {

    /**
     * 获取或设置缓存大小。
     */
    public int cacheSize = 100;

    protected LinkedHashMap<String, ItemEntry> entries;

    public LruCacheProvider() {
        entries = new LinkedHashMap<String, ItemEntry>(16, 0.75F, true) {
            // (an anonymous inner class)  
            private static final long serialVersionUID = 1;

            @Override
            protected boolean removeEldestEntry(Map.Entry<String, ItemEntry> eldest) {
                return size() > LruCacheProvider.this.cacheSize;
            }
        };
    }

    @Override
    public void set(String key, Object value, long timeout, boolean update, Callback<CacheEntry, Object> callback) throws InvalidOperationException {

        ItemEntry entry;
        if (entries.containsKey(key)) {
            entry = entries.get(key);
        } else {
            entry = new ItemEntry();
            entry.key = key;
            entries.put(key, entry);

        }

        entry.callback = callback;
        entry.visited = Instant.now().toEpochMilli();
        entry.timeout = timeout;
        entry.value = value;
        entry.canUpdate = update;

    }

    @Override
    public boolean set(String key, String index, Object value) {

        CacheEntry entry = this.get(key);
        if (entry == null || entry.getValue() == null || !(entry.getValue() instanceof Map)) {
            return false;
        }
        Map map = (Map) entry.getValue();
        map.put(index, value);
        return true;
    }

    @Override
    public boolean contains(String key) {
        this.get(key);//验证是否过期。
        return entries.containsKey(key);
    }

    @Override
    public CacheEntry get(String key) {
        ItemEntry entry = null;
        if (entries.containsKey(key)) {
            entry = entries.get(key);
            if (entry != null && entry.isExpired()) {
                this.remove(key);
                entry = null;
            } else if (entry != null) {
                entry.visited = Instant.now().toEpochMilli();
            }
        }
        return entry;
    }

    @Override
    public Object get(String key, String index) {
        CacheEntry entry = this.get(key);
        if (entry == null || entry.getValue() == null || !(entry.getValue() instanceof Map)) {
            return null;
        }
        Map map = (Map) entry.getValue();
        if (map.containsKey(index)) {
            return map.get(index);
        }
        return null;
    }

    @Override
    public void remove(String key) {
        entries.remove(key);
    }

    @Override
    public void remove(String key, String index) {
        CacheEntry entry = this.get(key);
        if (entry == null || entry.getValue() == null || !(entry.getValue() instanceof Map)) {
            return;
        }
        Map map = (Map) entry.getValue();
        map.remove(index);
    }

    @Override
    public void flush() {
    }

}
