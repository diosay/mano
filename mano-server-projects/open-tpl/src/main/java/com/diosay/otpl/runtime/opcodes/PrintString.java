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
 * 打印字符串
 *
 * @author jun <jun@diosay.com>
 */
public class PrintString extends OpCode {

    private byte[] array;
    private int offset;
    private int length;
    private String str;

    /**
     * 设置字符串
     *
     * @param s
     */
    public void setString(String s) {
        array = null;
        str = s;
    }

    /**
     * 设置字节组
     *
     * @param array
     * @param offset
     * @param length
     */
    public void setBytes(byte[] array, int offset, int length) {
        str = null;
        this.array = array;
        this.offset = offset;
        this.length = length;
    }

    @Override
    public OpcodeType getType() {
        return OpcodeType.ptstr;
    }

    @Override
    public void compile(OutputStream output, Charset encoding) throws Exception {
        if (array == null && str != null) {
            array = str.getBytes(encoding);
            offset = 0;
            length = array.length;
        }
        if (array == null) {
            throw new UnsupportedOperationException("array required.");
        }

        output.write(Utility.toBytes(this.getAddress()));
        output.write(this.getType().value);
        output.write(Utility.toBytes(length));
        output.write(array, offset, length);
    }

    @Override
    public int execute(ExecutionContext context) throws Exception {
        if (array == null && str != null) {
            array = str.getBytes(context.outputEncoding());
            offset = 0;
            length = array.length;
        } else if (!context.inputEncoding().equals(context.outputEncoding())) {
            str = new String(array, offset, length, context.inputEncoding());
            array = str.getBytes(context.outputEncoding());
            offset = 0;
            length = array.length;
        }

        context.write(false, array, offset, length);
        return this.getAddress() + 1;
    }

}
