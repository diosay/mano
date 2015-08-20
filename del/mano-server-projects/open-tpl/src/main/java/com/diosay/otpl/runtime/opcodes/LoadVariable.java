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
 * 载入一个变量
 * @author jun <jun@diosay.com>
 */
public class LoadVariable extends OpCode {

    private String name;

    /**
     * 设置变量名
     * @param n 
     */
    public LoadVariable setName(String n) {
        this.name = n;
        return this;
    }

    @Override
    public OpcodeType getType() {
        return OpcodeType.ldv;
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
        context.push(context.get(name));
        return this.getAddress() + 1;
    }

}
