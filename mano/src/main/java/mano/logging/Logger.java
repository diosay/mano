/*
 * Copyright (C) 2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.logging;

/**
 * 定义记录日志时使用的接口。
 * @author johnwhang
 */
public interface Logger {
    /**
     * @return 获取一个值，以判断 DEBUG 级别是否启用。 
     */
    boolean isDebugEnabled();
    /**
     * @return 获取一个值，以判断 INFO 级别是否启用。 
     */
    boolean isInfoEnabled();
    /**
     * @return 获取一个值，以判断 WARN 级别是否启用。 
     */
    boolean isWarnEnabled();
    /**
     * @return 获取一个值，以判断 ERROR 级别是否启用。 
     */
    boolean isErrorEnabled();
    /**
     * @return 获取一个值，以判断 INFO 级别是否启用。 
     */
    boolean isFatalEnabled();
    /**
     * @return 获取一个值，以判断 TRACE 级别是否启用。 
     */
    boolean isTraceEnabled();
    
    /**
     * 记录用于调试的信息。
     * @param message
     * @param causes 
     */
    void debug(CharSequence message, Throwable...causes);
    /**
     * 记录用于调试的信息。
     * @param cause 
     */
    void debug(Throwable cause);
    /**
     * 记录用于反馈特定的信息。
     * @param message
     * @param causes 
     */
    void info(CharSequence message, Throwable...causes);
     /**
     * 记录用于反馈特定的信息。
     * @param cause 
     */
    void info(Throwable cause);
    /**
     * 记录一些非错误的警告信息。
     * @param message
     * @param causes 
     */
    void warn(CharSequence message, Throwable...causes);
    /**
     * 记录一些非错误的警告信息。
     * @param cause 
     */
    void warn(Throwable cause);
    /**
     * 记录发生错误但能继续运行的信息。
     * @param message
     * @param causes 
     */
    void error(CharSequence message, Throwable...causes);
    /**
     * 记录发生错误但能继续运行的信息。
     * @param cause 
     */
    void error(Throwable cause);
    /**
     * 记录发生错误并且不能继续运行的致命信息。
     * @param message
     * @param causes 
     */
    void fatal(CharSequence message, Throwable...causes);
    /**
     * 记录发生错误并且不能继续运行的致命信息。
     * @param cause 
     */
    void fatal(Throwable cause);
    
    /**
     * 记录用于诊断程序运行的信息。
     * @param message
     * @param causes 
     */
    void trace(CharSequence message, Throwable...causes);
    /**
     * 记录用于诊断程序运行的信息。
     * @param cause 
     */
    void trace(Throwable cause);
}
