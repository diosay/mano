/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.logging;

import java.io.Closeable;
import java.io.Flushable;

/**
 * 日志处理程序抽象类。
 *
 * @author jun
 */
public abstract class Handler implements Closeable, Flushable {

    private volatile Formatter formatter = SimpleFormatter.DEFAULT;

    /**
     * 记录日志。
     *
     * @param entry
     * @return
     * @throws Exception
     */
    public final boolean log(Entry entry) throws Exception {
        if (isLoggable(entry)) {
            logBefor(entry);
            boolean result = doLog(entry);
            logAfter(entry);
            return result;
        }
        return false;
    }
    
    public void setProperty(String key,Object value){
        
    }

    /**
     * 记录前调用
     *
     * @param entry
     */
    protected void logBefor(Entry entry) {

    }

    /**
     * 记录后调用
     *
     * @param entry
     */
    protected void logAfter(Entry entry) {

    }
    
    

    /**
     * 检查当前项是否可记录。
     *
     * @param entry
     * @return
     */
    protected boolean isLoggable(Entry entry) {
        if (entry == null || entry.getLogger() == null) {
            return false;
        }
        else if (Level.ALL == entry.getLevel()) {
            return true;
        } else if (Level.OFF == entry.getLevel()) {
            return false;
        } else if (Level.DEBUG == entry.getLevel()) {
            return entry.getLogger().isDebugEnabled();
        } else if (Level.ERROR == entry.getLevel()) {
            return entry.getLogger().isErrorEnabled();
        } else if (Level.FATAL == entry.getLevel()) {
            return entry.getLogger().isFatalEnabled();
        } else if (Level.INFO == entry.getLevel()) {
            return entry.getLogger().isInfoEnabled();
        } else if (Level.TRACE == entry.getLevel()) {
            return entry.getLogger().isTraceEnabled();
        } else if (Level.WARNING == entry.getLevel()) {
            return entry.getLogger().isWarnEnabled();
        }
        return false;
    }

    /**
     * 执行记录。
     *
     * @param entry
     * @return
     * @throws Exception
     */
    protected abstract boolean doLog(Entry entry) throws Exception;

    /**
     * 设置一个新的格式化程序
     *
     * @param newFormatter
     */
    public synchronized void setFormatter(Formatter newFormatter) {
        if (newFormatter == null) {
            throw new NullPointerException("newFormatter");
        }
        formatter = newFormatter;
    }

    /**
     * 获取日志格式化程序。
     *
     * @return
     */
    public Formatter getFormatter() {
        return formatter;
    }

}
