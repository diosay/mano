/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.logging;

import mano.DateTime;

/**
 * 表示一条日志记录。
 * @author johnwhang
 */
public interface Entry {
    
    /**
     * 获取处理程序。
     * @return 
     */
    Handler getHandler();
    
    /**
     * 获取日志的类别。
     * 如：错误日志(Error log)、访问日志(Access log)等。
     */
    String getCategory();
    
    /**
     * 获取日志的级别。
     * @see Level
     */
    Level getLevel();
    
    /**
     * 获取日志发生的时间。
     */
    DateTime getTime();
    
    /**
     * 获取日志消息。
     */
    CharSequence getMessage();
    
    /**
     * 获取随带日志的附加异常对象。
     */
    Throwable[] getExceptions();
    
    /**
     * 获取记录日志时的类名。
     */
    String getSourceClassName();
    
    /**
     * 获取记录日志时的方法名称。
     */
    String getSourceMethodName();
    
    /**
     * 获取记录日志时的行号。
     */
    long getSourceLineNumber();
    
    /**
     * 获取记录日志时的线程ID。
     */
    long getSourceThreadId();
    
    /**
     * 获取创建该实例的日志器。
     * @return 
     */
    Logger getLogger();
    //增加成功处理次数
    //获取成功处理次数
}
