/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package com.diosay.otpl.runtime.opcodes;

import com.diosay.otpl.runtime.ExecutionContext;
import com.diosay.otpl.runtime.Interpreter;
import com.diosay.otpl.runtime.OpCode;
import com.diosay.otpl.runtime.OpcodeType;
import java.io.OutputStream;
import java.nio.charset.Charset;
import mano.util.Utility;

/**
 * 调用一个块。
 *
 * @author jun <jun@diosay.com>
 */
public class CallBlock extends OpCode {

    private boolean required;
    private String name;

    public void required(boolean b) {
        required = b;
    }

    public void setName(String n) {
        name = n;
    }

    @Override
    public OpcodeType getType() {
        return OpcodeType.call_blk;
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
            throw new UnsupportedOperationException("未设置块名.");
        }
        output.write(required ? 1 : 0);
    }

    @Override
    public int execute(ExecutionContext context) throws Exception {

        Block blk = this.getLoader().getBlock(name);
        if (blk == null && required) {
            throw new UnsupportedOperationException("块 "+name +" 是必须的，但未找到");
        }else if (blk == null){
            return this.getAddress()+1;
        }
        
        try (Interpreter interpreter = context.newInterpreter()) {
            interpreter.exec(context, blk.getLoader(),blk.getBeginAddress(),blk.getEndAddress());
            context.freeInterpreter(interpreter);
        }
        
        return this.getAddress()+1;
    }

}
