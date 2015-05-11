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
 * 弹出元素
 *
 * @author jun <jun@diosay.com>
 */
public class Pop extends OpCode {

    @Override
    public OpcodeType getType() {
        return OpcodeType.pop;
    }

    @Override
    public void compile(OutputStream output, Charset encoding) throws Exception {
        output.write(Utility.toBytes(this.getAddress()));
        output.write(this.getType().value);
    }

    @Override
    public int execute(ExecutionContext context) throws Exception {
        context.pop();
        return this.getAddress() + 1;
    }

}
