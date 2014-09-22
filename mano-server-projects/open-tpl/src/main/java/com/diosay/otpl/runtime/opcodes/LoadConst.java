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
 * 载入常量
 *
 * @author jun <jun@diosay.com>
 */
public class LoadConst extends OpCode {

    public static byte NULL = 0;
    public static byte FALSE = 1;
    public static byte TRUE = 2;
    private byte value;

    public LoadConst setValue(byte val) {
        this.value = val;
        return this;
    }

    @Override
    public OpcodeType getType() {
        return OpcodeType.ldc;
    }

    @Override
    public void compile(OutputStream output, Charset encoding) throws Exception {
        output.write(Utility.toBytes(this.getAddress()));
        output.write(this.getType().value);
        output.write(value);
    }

    @Override
    public int execute(ExecutionContext context) throws Exception {
        if (value == NULL) {
            context.push(null);
        } else if (value == FALSE) {
            context.push(false);
        }
        else if (value == TRUE) {
            context.push(true);
        } else {
            throw new java.lang.RuntimeException("常量类型未定义:" + value);
        }
        return this.getNextAddress();
    }

}
