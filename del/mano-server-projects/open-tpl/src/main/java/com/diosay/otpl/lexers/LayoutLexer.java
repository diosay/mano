/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */

package com.diosay.otpl.lexers;

import com.diosay.otpl.Document;
import com.diosay.otpl.Lexer;
import com.diosay.otpl.Node;
import com.diosay.otpl.Span;
import com.diosay.otpl.runtime.OpCode;
import com.diosay.otpl.runtime.OpcodeType;
import com.diosay.otpl.runtime.opcodes.Layout;
import com.diosay.otpl.runtime.opcodes.Print;
import java.util.ArrayList;

/**
 * 布局
 * @author jun <jun@diosay.com>
 */
public class LayoutLexer  extends com.diosay.otpl.Compiler implements Lexer{

    @Override
    public void compile(Node node, ArrayList<OpCode> list) {
        int size=list.size();
        this.compile(node.getSource(), list,node.getLineNumber());
        if(list.size()==size){
            throw new mano.InvalidOperationException("未设置布局文件名称.");
        }
        Layout code = new Layout();
        list.add(code);
    }

    @Override
    public String getToken() {
        return "layout";
    }

    @Override
    public boolean isBlock() {
        return false;
    }

    @Override
    public void parse(Document dom, CharSequence source, int start, int end, int line) {
        Span node=new Span(this.getToken());
        node.setCompiler(this);
        node.setLineNumber(line);
        node.setSource(source, start, end);
        dom.append(node);
        
    }
    
}
