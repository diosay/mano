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
 * 打印对象
 *
 * @author jun <jun@diosay.com>
 */
public class Print extends OpCode {

    private boolean filtrable;

    /**
     * 设置一否过滤，默认真
     *
     * @param b
     */
    public void setFiltrable(boolean b) {
        filtrable = b;
    }

    public boolean getFiltrable() {
        return filtrable;
    }

    @Override
    public OpcodeType getType() {
        return OpcodeType.prt;
    }

    @Override
    public void compile(OutputStream output, Charset encoding) throws Exception {
        output.write(Utility.toBytes(this.getAddress()));
        output.write(this.getType().value);
        output.write(getFiltrable() ? 1 : 0);
    }

    @Override
    public int execute(ExecutionContext context) throws Exception {
        
        Object obj=context.pop();
        if(obj!=null){
            context.write(getFiltrable(),obj);
        }
        
        return this.getAddress()+1;
        
    }

}
