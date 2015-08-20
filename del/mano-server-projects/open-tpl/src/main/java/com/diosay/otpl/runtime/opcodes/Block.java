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
 * 块
 *
 * @author jun <jun@diosay.com>
 */
public class Block extends OpCode {

    private OpCode begin;
    private int beginAddr;

    private String name;

    /**
     * 设置变量名
     *
     * @param n
     */
    public void setName(String n) {
        this.name = n;
    }

    public void setBegin(OpCode start) {
        begin = start;
    }

    public void setBeginAddress(int addr) {
        beginAddr = addr;
    }

    public int getBeginAddress() {
        return beginAddr;
    }

    public int getEndAddress() {
        return this.getAddress() - 1;
    }

    public String getName() {
        return name;
    }

    @Override
    public OpcodeType getType() {
        return OpcodeType.stblk;
    }

    @Override
    public void compile(OutputStream output, Charset encoding) throws Exception {
        output.write(Utility.toBytes(this.getAddress()));
        output.write(this.getType().value);
        output.write(Utility.toBytes(begin.getAddress()));
        if (this.name != null) {
            byte[] bytes = this.name.getBytes(encoding);
            output.write(Utility.toBytes(bytes.length));
            output.write(bytes);
        } else {
            throw new UnsupportedOperationException("未设置块名.");
        }
    }

    @Override
    public int execute(ExecutionContext context) throws Exception {//
        throw new UnsupportedOperationException("Not supported yet.");//apply
    }

}
