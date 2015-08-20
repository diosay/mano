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
 * 设置变量值
 * @author jun <jun@diosay.com>
 */
public class SetVariable extends OpCode {

    private String name;

    /**
     * 设置变量名
     * @param n 
     */
    public SetVariable setName(String n) {
        this.name = n;
        return this;
    }
    public String getName(){
        return name;
    }
    
    @Override
    public OpcodeType getType() {
        return OpcodeType.stv;
    }

    @Override
    public void compile(OutputStream output, Charset encoding) throws Exception {
        output.write(Utility.toBytes(this.getAddress()));
        output.write(this.getType().value);
        if (this.name != null) {
            byte[] bytes = this.name.getBytes(encoding);
            output.write(Utility.toBytes(bytes.length));
            output.write(bytes);
        } else {
            throw new UnsupportedOperationException("未设置变量名.");
        }
    }

    @Override
    public int execute(ExecutionContext context) throws Exception {
        context.set(name,context.pop());
        return this.getAddress() + 1;
    }
    
}
