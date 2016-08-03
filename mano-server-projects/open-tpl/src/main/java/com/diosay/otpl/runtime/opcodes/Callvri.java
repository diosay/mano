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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import mano.util.Utility;

/**
 * 调用虚函数或内建函数。
 *
 * @author jun <jun@diosay.com>
 */
public class Callvri extends OpCode {

    private String name;
    private int len;

    public Callvri setName(String n) {
        name = n;
        return this;
    }

    public Callvri setArgLength(int n) {
        len = n;
        return this;
    }

    @Override
    public OpcodeType getType() {
        return OpcodeType.callvri;
    }

    @Override
    public void compile(OutputStream output, Charset encoding) throws Exception {
        output.write(Utility.toBytes(this.getAddress()));
        output.write(this.getType().value);
        output.write(Utility.toBytes(this.len));
        if (this.name != null) {
            byte[] bytes = this.name.getBytes(encoding);
            output.write(Utility.toBytes(bytes.length));
            output.write(bytes);
        } else {
            throw new UnsupportedOperationException("未设置函数名.");
        }
    }

    @Override
    public int execute(ExecutionContext context) throws Exception {
        if (this.name == null || "".equals(this.name)) {
            throw new UnsupportedOperationException("未设置函数名.");
        }

        ArrayList<Object> args = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            args.add(context.pop());
        }
        Collections.reverse(args);
        context.push(context.call(this.name, args.toArray()));
        return this.getNextAddress();
    }

}
