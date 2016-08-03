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
import static com.diosay.otpl.runtime.OpCode.DEBUG;
import com.diosay.otpl.runtime.OpcodeType;
import java.io.OutputStream;
import java.nio.charset.Charset;
import mano.util.Utility;

/**
 * 载入一个小数
 * @author jun <jun@diosay.com>
 */
public class LoadReal extends OpCode {

    private double value;
    public void setValue(double val){
        this.value=val;
    }
    
    @Override
    public OpcodeType getType() {
        return OpcodeType.ldr;
    }

    @Override
    public void compile(OutputStream output, Charset encoding) throws Exception {
        output.write(Utility.toBytes(this.getAddress()));
        output.write(this.getType().value);
        output.write(Utility.toBytes(value));
        
    }

    @Override
    public int execute(ExecutionContext context) throws Exception {
        context.push(value);
        return this.getAddress()+1;
    }
    
}
