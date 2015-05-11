/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package com.diosay.otpl.runtime;

import com.diosay.otpl.runtime.opcodes.*;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * 表示一条Open-TPL操作指令。
 *
 * @author jun <jun@diosay.com>
 */
public abstract class OpCode {

    public static final boolean DEBUG=true;
    
    private int addr;
    @Deprecated
    private int lineNumber;
    private CodeLoader loader;
    /**
     * 获取当前指令的地址。
     * @return 
     */
    public final int getAddress(){
        return this.addr;
    }
    /**
     * 获取下条指令的地址。
     * @return 
     */
    public final int getNextAddress(){
        return this.addr+1;
    }
    /**
     * 设置当前指令的地址。
     * @param address 
     */
    public final void setAddress(int address){
        this.addr=address;
    }
    
    /**
     * 获取源代码行号
     * @return 
     */
    @Deprecated
    public final int getSourceLine(){
        return this.lineNumber;
    }
    /**
     * 置源代码行号
     * @param line 
     */
    @Deprecated
    public final OpCode setSourceLine(int line){
        this.lineNumber=line;
        return this;
    }
    
    /**
     * 获取当前的操作码
     * @return 
     */
    public abstract OpcodeType getType();
    
    /**
     * 编译并输出。
     * <p>注意：编码格式必须是UTF8.
     * @param output 
     */
    public abstract void compile(OutputStream output,Charset encoding) throws Exception;

    /**
     * 执行该指令,并返回下条指令地址
     * @param context 
     */
    public abstract int execute(ExecutionContext context) throws Exception;

    
    /**
     * 创建一个新标签。
     * @return 
     */
    public static OpCode label(){
        return new Nop();
    }
    
    /**
     * 创建一个新的中断指令。
     * @param target
     * @return 
     */
    public static Break makeBreak(OpCode target){
        return new Break().setTarget(target).setBehavior(Break.BREAK);
    }
    /**
     * 创建一个新的中断指令。
     * @param target
     * @return 
     */
    public static Break makeBreakTrue(OpCode target){
        return new Break().setTarget(target).setBehavior(Break.BREAK_TRUE);
    }
    /**
     * 创建一个新的中断指令。
     * @param target
     * @return 
     */
    public static Break makeBreakFalse(OpCode target){
        return new Break().setTarget(target).setBehavior(Break.BREAK_FALSE);
    }
    /**
     * 创建一个新的中断指令。
     * @return 
     */
    public static Break makeBreakReturn(){
        return new Break().setBehavior(Break.BREAK_EXIT);
    }
    /**
     * 创建一个新的中断指令。
     * @param target
     * @return 
     */
    public static Break makeBreak(int target){
        return new Break().setTarget(target).setBehavior(Break.BREAK);
    }
    /**
     * 创建一个新的中断指令。
     * @param target
     * @return 
     */
    public static Break makeBreakTrue(int target){
        return new Break().setTarget(target).setBehavior(Break.BREAK_TRUE);
    }
    /**
     * 创建一个新的中断指令。
     * @param target
     * @return 
     */
    public static Break makeBreakFalse(int target){
        return new Break().setTarget(target).setBehavior(Break.BREAK_FALSE);
    }
    /**
     * 设置与之关联的载入器
     * @param loader 
     */
    public void setLoader(CodeLoader loader){
        this.loader=loader;
    }
    
    /**
     * 获取与之关联的载入器
     * @return 
     */
    public CodeLoader getLoader(){
        return loader;
    }
    
    public String tag;
    public String mark;
}