/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */

package com.diosay.otpl.runtime.opcodes;

import com.diosay.otpl.runtime.ExecutionContext;
import com.diosay.otpl.runtime.Interpreter;
import com.diosay.otpl.runtime.OpCode;
import static com.diosay.otpl.runtime.OpCode.DEBUG;
import com.diosay.otpl.runtime.OpcodeType;
import java.io.OutputStream;
import java.nio.charset.Charset;
import mano.util.Utility;

/**
 * 正文
 * @author jun <jun@diosay.com>
 */
public class Body extends OpCode {

    @Override
    public OpcodeType getType() {
        return OpcodeType.body;
    }

    @Override
    public void compile(OutputStream output, Charset encoding) throws Exception {
        output.write(Utility.toBytes(this.getAddress()));
        output.write(this.getType().value);
        
    }

    @Override
    public int execute(ExecutionContext context) throws Exception {
        
        if(this.getLoader().child==null){
            throw new mano.InvalidOperationException("不是一个效的布局页面，或未找到子页面。");
        }
        
        Interpreter interpreter = context.newInterpreter();
        
        interpreter.exec(context, this.getLoader().child,this.getLoader().pageAddr,-1);//执行全部子页面
        
        context.freeInterpreter(interpreter);
        
        
        return this.getAddress()+1;
    }
    
}
