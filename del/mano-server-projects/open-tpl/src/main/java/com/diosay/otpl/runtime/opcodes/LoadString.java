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
 * 载入字符串
 *
 * @author jun <jun@diosay.com>
 */
public class LoadString extends OpCode {

    private String str;
    public void setContent(String s) {
        str = s;
    }

    public String getContent() {
        return str;
    }

    @Override
    public OpcodeType getType() {
        return OpcodeType.ldstr;
    }

    @Override
    public void compile(OutputStream output, Charset encoding) throws Exception {
        output.write(Utility.toBytes(this.getAddress()));
        output.write(this.getType().value);
        if (this.getContent() == null) {
            output.write(Utility.toBytes(0));
        } else {
            byte[] bytes = this.getContent().getBytes(encoding);
            output.write(Utility.toBytes(bytes.length));
            output.write(bytes);
        }
    }

    @Override
    public int execute(ExecutionContext context) throws Exception {
        context.push(this.getContent());
        return this.getAddress()+1;
    }

}
