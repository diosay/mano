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
 * 如果 value 为 false、空引用或零，则将控制转移到目标指令。
 * @author jun <jun@diosay.com>
 */
@java.lang.Deprecated
public class BreakFalse extends OpCode {

    private OpCode target;
    
    /**
     * 获取条件满足时跳转的地址。
     * @return 
     */
    public OpCode getTarget(){
        return target;
    }
    
    /**
     * 获取条件满足时跳转的地址。
     * @return 
     */
    public BreakFalse setTarget(OpCode target){
        this.target=target;
        return this;
    }
    
    @Override
    public final OpcodeType getType() {
        return OpcodeType.brf;
    }

    @Override
    public void compile(OutputStream output,Charset encoding) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int execute(ExecutionContext context) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
