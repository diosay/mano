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
import mano.util.Utility;

/**
 * 表示一个占位，不执行任何操作。通常用作 label 使用。
 * @author jun <jun@diosay.com>
 */
public class Nop extends OpCode {

    public Nop(){}
    public Nop(int line){
        this.setSourceLine(line);
    }
    
    /**
     * 编译的辅助标记。
     */
    public String mark;
    
    @Override
    public final OpcodeType getType() {
        return OpcodeType.nop;
    }
    
    @Override
    public void compile(OutputStream output,Charset encoding) throws Exception {
        
        output.write(Utility.toBytes(this.getAddress()));
        output.write(this.getType().value);
        
        
    }

    @Override
    public int execute(ExecutionContext context) throws Exception {
        //nothing
        return this.getAddress()+1;
    }

    

  

}
