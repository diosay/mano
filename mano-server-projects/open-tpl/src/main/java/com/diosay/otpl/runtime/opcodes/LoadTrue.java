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

/**
 * 载入常量 true
 * @author jun <jun@diosay.com>
 */
public class LoadTrue extends OpCode {

    @Override
    public OpcodeType getType() {
        return OpcodeType.ldc;//TODO
    }

    @Override
    public void compile(OutputStream output, Charset encoding) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int execute(ExecutionContext context) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
