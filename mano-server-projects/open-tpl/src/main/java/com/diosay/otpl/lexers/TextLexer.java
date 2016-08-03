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
import com.diosay.otpl.runtime.OpCode;
import com.diosay.otpl.runtime.opcodes.Print;
import com.diosay.otpl.runtime.opcodes.PrintString;
import java.util.ArrayList;

/**
 * 实现文本的编译。
 * @author jun <jun@diosay.com>
 */
public class TextLexer extends com.diosay.otpl.Compiler implements Lexer {

    @Override
    public void compile(Node node, ArrayList<OpCode> list) {
        PrintString code=new PrintString();
        code.setString(node.getSource().toString());
        list.add(code);
    }

    @Override
    public String getToken() {
        return "text";
    }

    @Override
    public boolean isBlock() {
        return false;
    }

    @Override
    public void parse(Document dom, CharSequence source, int start, int end, int line) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
