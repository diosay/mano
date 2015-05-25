/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.caching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;
import mano.Action;
import mano.InvalidOperationException;

/**
 * 通过维护一个 HashMap 来实现的简单缓存提供程序。
 *
 * @author jun <jun@diosay.com>
 * @param <V>
 */
public class HashCacheProvider<V extends Object> implements CacheProvider<V> {

    /**
     * 实现缓存的项。
     *
     * @author jun <jun@diosay.com>
     */
    protected class ItemEntry implements CacheEntry<V>, Map.Entry<String, V> {

        String key;
        V value;
        long timeout;
        long visited;
        boolean canUpdate;
        Action<CacheEntry> callback;

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public long getLastVisited() {
            return visited;
        }

        @Override
        public long getTimeout() {
            return timeout;
        }

        @Override
        public boolean canUpdate() {
            return canUpdate;
        }

        @Override
        public boolean isExpired() {
            return System.currentTimeMillis() - visited > timeout;
        }

        @Override
        public V setValue(V value) {
            this.value = value;
            return value;
        }

    }

    protected final Map<String, ItemEntry> cached;

    protected HashCacheProvider(Map<String, ItemEntry> entries) {
        if (entries == null) {
            throw new java.lang.NullPointerException("entries");
        }
        this.cached = entries;
    }

    public HashCacheProvider() {
        this(new HashMap<>());
    }

    public int size() {
        return cached.size();
    }

    @Override
    public void set(String key, V value, long timeout, boolean update, Action<CacheEntry> callback) throws InvalidOperationException {
        ItemEntry entry;
        if (cached.containsKey(key)) {
            entry = cached.get(key);
        } else {
            entry = new ItemEntry();
            entry.key = key;
            cached.put(key, entry);
        }

        entry.callback = callback;
        entry.visited = System.currentTimeMillis();
        entry.timeout = timeout;
        entry.value = value;
        entry.canUpdate = update;

    }

    @Override
    public V get(String key) {
        CacheEntry<V> entry = getEntry(key);
        return entry == null ? null : entry.getValue();
    }

    @Override
    public CacheEntry<V> getEntry(String key) {
        ItemEntry entry = cached.getOrDefault(key, null);
        if (entry != null && entry.isExpired()) {
            this.remove(key);
            entry = null;
        } else if (entry != null) {
            entry.visited = System.currentTimeMillis();
        }
        return entry;
    }

    @Override
    public CacheEntry<V> remove(String key) {
        ItemEntry entry = cached.remove(key);
        if (entry != null && entry.callback != null) {
            entry.callback.run(entry);
        }
        return entry;
    }

    @Override
    public void flush() {

    }

    @Override
    public Object get(String key, String index) {
        Map map = getSubValueMap(key);
        if (map != null) {
            return map.getOrDefault(index, null);
        }
        return null;
    }

    @Override
    public boolean contains(String key) {
        return this.get(key) != null;
    }

    @Override
    public boolean set(String key, String index, Object value) {
        Map map = getSubValueMap(key);
        if (map != null) {
            map.put(index, value);
            return true;
        }
        return false;
    }

    @Override
    public void remove(String key, String index) {
        Map map = getSubValueMap(key);
        if (map != null) {
            map.remove(index);
        }
    }

    private Map getSubValueMap(String key) {
        CacheEntry entry = this.getEntry(key);
        if (entry == null || entry.getValue() == null || !(entry.getValue() instanceof Map)) {
            return null;
        }
        return (Map) entry.getValue();
    }

    
    public void forEach(Action<CacheEntry> action){
        //Iterator<ItemEntry> iter;
        new ArrayList<>(this.cached.values()).forEach(item->{
            action.run(item);
        });
//        while(iter.hasNext()){
//            action.run(iter.next());
//            //iter.remove();
//        }
    }
    
}
