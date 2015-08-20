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
import com.diosay.otpl.runtime.opcodes.BreakDescriptor;
import java.util.ArrayList;

/**
 * 中断语句
 * @author jun <jun@diosay.com>
 */
public class ContinueLexer extends com.diosay.otpl.Compiler implements Lexer {

    @Override
    public void compile(Node node, ArrayList<OpCode> list) {
        list.add(new BreakDescriptor());
    }

    @Override
    public String getToken() {
        return "continue";
    }

    @Override
    public boolean isBlock() {
        return false;
    }

    @Override
    public void parse(Document dom, CharSequence source, int start, int end, int line) {
        Span node = new Span(this.getToken());
        node.setCompiler(this);
        node.setLineNumber(line);
        node.setSource("", 0, 0);
        dom.append(node);
    }

}
