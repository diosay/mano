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
 * 定义一个源代码行号。
 *
 * @author jun <jun@diosay.com>
 */
public class SourceLineNumber extends OpCode {

    private int lineNumber;

    public SourceLineNumber setValue(int line) {
        lineNumber = line;
        return this;
    }

    @Override
    public OpcodeType getType() {
        return OpcodeType.sl;
    }

    @Override
    public void compile(OutputStream output, Charset encoding) throws Exception {
        output.write(Utility.toBytes(this.getAddress()));
        output.write(this.getType().value);
        output.write(Utility.toBytes(lineNumber));
    }

    @Override
    public int execute(ExecutionContext context) throws Exception {
        context.setCurrentSourceLine(lineNumber);
        return this.getNextAddress();
    }

}
