/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package com.diosay.otpl.lexers;

import com.diosay.otpl.Block;
import com.diosay.otpl.Document;
import com.diosay.otpl.Lexer;
import com.diosay.otpl.Node;
import com.diosay.otpl.runtime.OpCode;
import java.util.ArrayList;

/**
 * 例外条件
 *
 * @author jun <jun@diosay.com>
 */
public class ElifLexer extends com.diosay.otpl.Compiler implements Lexer {

    @Override
    public void compile(Node node, ArrayList<OpCode> list) {
        //nothing
    }

    @Override
    public String getToken() {
        return "elif";
    }

    @Override
    public boolean isBlock() {
        return true;
    }

    @Override
    public void parse(Document dom, CharSequence source, int start, int end, int line) {
        Block node = dom.createBlock(this.getToken());
        node.setCompiler(this);
        node.setSource(source, start, end);
        node.setLineNumber(line);
        node.setCloseName("if", "else","elif");
        dom.append(node);
    }

}
