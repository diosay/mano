/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */

package com.diosay.otpl.runtime.opcodes;

import com.diosay.otpl.runtime.ExecutionContext;
import com.diosay.otpl.runtime.OpCode;
import com.diosay.otpl.runtime.OpcodeType;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * 中断指令描述符。
 * 注意：该指令属于辅助指令(属于jmp的代理指令)，只用于编译阶段，不具体生成和执行。
 * @author jun <jun@diosay.com>
 */
public class BreakDescriptor extends OpCode {

    public OpCode target;
    
    /**
     * true 表示 break ，false 表示 continue。默认 flase。
     */
    public boolean blocked;
    
    @Override
    public OpcodeType getType() {
        return OpcodeType.brd; 
    }

    @Override
    public void compile(OutputStream output, Charset encoding) throws Exception {
        Break jmp=new Break();
        jmp.setBehavior(Break.BREAK);
        jmp.setTarget(target);
        jmp.setAddress(this.getAddress());
        jmp.compile(output, encoding);
    }

    @Override
    public int execute(ExecutionContext context) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
