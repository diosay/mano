/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */

package com.diosay.otpl.runtime.opcodes;

import com.diosay.otpl.runtime.CodeLoader;
import com.diosay.otpl.runtime.ExecutionContext;
import com.diosay.otpl.runtime.Interpreter;
import com.diosay.otpl.runtime.OpCode;
import com.diosay.otpl.runtime.OpcodeType;
import java.io.File;
import java.io.OutputStream;
import java.nio.charset.Charset;
import mano.util.Utility;

/**
 * 执行一个文档
 * @author jun <jun@diosay.com>
 */
public class Layout extends OpCode {
    
    @Override
    public OpcodeType getType() {
        return OpcodeType.doc;
    }

    @Override
    public void compile(OutputStream output, Charset encoding) throws Exception {
        output.write(Utility.toBytes(this.getAddress()));
        output.write(this.getType().value);
    }

    @Override
    public int execute(ExecutionContext context) throws Exception {
        
        Object obj=context.pop();
        if(obj==null){
            throw new mano.InvalidOperationException("未设置文件名称");
        }
        String filename=obj.toString().replace("\\", "/");
        
        if(filename.startsWith("~/")){
            filename=Utility.toPath(context.getBasedir(), filename.substring(2)).toString();
        }else if(filename.startsWith("/")){
            if(this.getLoader()==null || this.getLoader().getSource()==null){
                throw new mano.InvalidOperationException("当前代码未关联加载器或不是执行的文件。");
            }
            filename=Utility.toPath(this.getLoader().getSource().getParent(), filename.substring(1)).toString();
        }
        
        try (Interpreter interpreter = context.newInterpreter()) {
            this.getLoader().parent=context.getLoader(new File(filename), interpreter);
            this.getLoader().parent.pageAddr=this.getAddress()+1;
            this.getLoader().parent.child=this.getLoader();//TODO:循环引用
            interpreter.exec(context, this.getLoader().parent);
            context.freeInterpreter(interpreter);
        }
        return this.getAddress()+1;
    }
    
}
