/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package com.diosay.otpl.lexers;

import com.diosay.otpl.CompileException;
import com.diosay.otpl.Document;
import com.diosay.otpl.Lexer;
import com.diosay.otpl.Node;
import com.diosay.otpl.Span;
import com.diosay.otpl.StringUtil;
import com.diosay.otpl.Token;
import com.diosay.otpl.runtime.OpCode;
import com.diosay.otpl.runtime.opcodes.SetVariable;
import java.util.ArrayList;

/**
 * 设置变量的值
 *
 * @author jun <jun@diosay.com>
 */
public class VariableLexer extends com.diosay.otpl.Compiler implements Lexer {

    @Override
    public void compile(Node node, ArrayList<OpCode> list) {

        CharSequence source = node.getSource();

        if (source == null) {
            throw new CompileException("语法错误：未设置变量名称");
        }
        int index = 0;
        index = StringUtil.trimLeftWhitespace(source, index, source.length());

        if (source.charAt(index) != ':') {
            throw new CompileException("语法错误：未知字符");
        }
        index++;
        int index2 = StringUtil.findIdentifier(source, index, source.length());

        if (index2 < 0) {
            throw new CompileException("语法错误：未设置变量名称。");
        }
        String name = source.subSequence(index, index2).toString();

        if (this.isKeyword(name)) {
            throw new UnsupportedOperationException("语法错误：变量名不能是关键词：" + name);
        }
        ArrayList<Token> tokens = this.scan(source, index2, source.length(), node.getLineNumber());
        this.grammar(tokens);
        if (tokens.size() != 1) {
            throw new UnsupportedOperationException("语法错误：变量值太多。");
        }
        for (Token sub : tokens) {
            this.visit(sub, list);
        }
        list.add(new SetVariable().setName(name));

    }

    @Override
    public String getToken() {
        return "var";
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
