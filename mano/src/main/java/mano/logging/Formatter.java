/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.logging;

/**
 * 表示一个用于格式化日志的方法。
 * @author jun
 */
public interface Formatter {
    
    /**
     * 格式化一个日志项。
     * @param entry
     * @return 
     */
    CharSequence format(Entry entry);
}
