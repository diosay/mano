/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.caching;

import java.time.Instant;
import java.util.Map;
import javafx.util.Callback;

/**
 * 内部实现缓存的项。
 * @author jun <jun@diosay.com>
 */
class ItemEntry implements CacheEntry, Map.Entry<String, Object> {

    String key;
    Object value;
    long timeout;
    long visited;
    boolean canUpdate;
    Callback<CacheEntry, Object> callback;

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Object getValue() {
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
        //return false;
        return Instant.now().toEpochMilli() - visited > timeout;
    }

    @Override
    public Object setValue(Object value) {
        this.value = value;
        return value;
    }

}
