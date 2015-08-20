/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */

package com.diosay.otpl.runtime;

import com.diosay.otpl.CompilationContext;
import java.io.File;
import java.nio.charset.Charset;

/**
 * 表示一个执行上下文。
 * @author jun <jun@diosay.com>
 */
public interface ExecutionContext extends CompilationContext {
    
    
    /**
     * 获取输出编译。
     * @return 
     */
    Charset outputEncoding();
    
    /**
     * 获取输出编译。
     * @return 
     */
    Charset inputEncoding();
    
    /**
     * 设置一个变量
     * @param key
     * @param value 
     */
    void set(String key,Object value);
    /**
     * 获取一个变量
     * @param key
     * @return 
     */
    Object get(String key);
    
    /**
     * 压入对象到栈顶
     * @param value 
     */
    void push(Object value);
    /**
     * 从栈顶弹出一个对象，如果栈为空则返回 null
     * @return 
     */
    Object pop();
    
    /**
     * 从栈顶取出一个对象，如果栈为空则返回 null
     * @return 
     */
    Object peek();
    
    void write(boolean filtrable,Object obj);
    
    /**
     * 写入字序列。
     * @param filtrable
     * @param cs
     * @param start
     * @param end 
     */
    void write(boolean filtrable,CharSequence cs, int start, int end);

    /**
     * 写入二进制。
     * @param filtrable
     * @param array
     * @param index
     * @param count 
     */
    void write(boolean filtrable, byte[] array, int index, int count);
    
    /**
     * 清空变量、堆栈与重置相关运行时参数。
     */
    void reset();
    
    /**
     * 获取(或创建)一个新的解释器。
     * @return 
     */
    Interpreter newInterpreter();
    
    /**
     * 释放一个解释器，以便其他使用。
     * @param interpreter
     */
    void freeInterpreter(Interpreter interpreter);
    
    /**
     * 根据源文件获取加载器。
     * @param source
     * @param interpreter
     * @return 
     */
    CodeLoader getLoader(String source) throws Exception;
    
    /**
     * 设置当前源代码行号。
     * @param line 
     */
    void setCurrentSourceLine(int line);
    
    /**
     * 获取当前源代码行号。
     * @return 
     */
    int getCurrentSourceLine();
    
    /**
     * 获取内置函数接口。
     * @return 
     */
    BuiltinFunctionInterface calls();
    
    /**
     * 调用函数。
     * @param funcName
     * @param args
     * @return 
     */
    Object call(String funcName,Object[] args);
}
