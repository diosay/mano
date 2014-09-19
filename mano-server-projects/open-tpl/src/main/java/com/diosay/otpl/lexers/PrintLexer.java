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
import com.diosay.otpl.StringUtil;
import com.diosay.otpl.runtime.OpCode;
import com.diosay.otpl.runtime.opcodes.LoadVariable;
import com.diosay.otpl.runtime.opcodes.Print;
import java.util.ArrayList;

/**
 * 打印
 *
 * @author jun <jun@diosay.com>
 */
public class PrintLexer extends com.diosay.otpl.Compiler implements Lexer {

    @Override
    public void compile(Node node, ArrayList<OpCode> list) {
        CharSequence source = node.getSource();
        if (source == null || source.length() == 0) {
            return;
        }
        boolean filtrable = false;
        if ('@' == source.charAt(0)) {
            filtrable = true;
        }

        //TODO:格式化
        this.compile(filtrable ? source.subSequence(1, source.length() - 1) : source, list,node.getLineNumber());

//        LoadVariable var = new LoadVariable();
//        var.setName(source.subSequence(filtrable ? 1 : 0, source.length()).toString());
//        list.add(var);

        Print code = new Print();
        code.setFiltrable(filtrable);

        list.add(code);
    }

    @Override
    public String getToken() {
        return "print";
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
        node.setSource(source, start, end);
        dom.append(node);
    }

}
