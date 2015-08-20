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
import com.diosay.otpl.StringUtil;
import com.diosay.otpl.runtime.OpCode;
import java.util.ArrayList;

/**
 * 块定义词条
 *
 * @author jun <jun@diosay.com>
 */
public class SectionLexer extends com.diosay.otpl.Compiler implements Lexer {

    @Override
    public void compile(Node node, ArrayList<OpCode> list) {
        CharSequence source = node.getSource();

        if (source == null) {
            throw new UnsupportedOperationException("语法错误：未设置名称");
        }
        int index = 0;
        index = StringUtil.trimLeftWhitespace(source, index, source.length());

        if (source.charAt(index) != ':') {
            throw new UnsupportedOperationException("语法错误：未知字符");
        }
        index++;
        int index2 = StringUtil.findIdentifier(source, index, source.length());

        if (index2 < 0) {
            throw new UnsupportedOperationException("语法错误：未设置名称");
        }
        String name = source.subSequence(index, index2).toString();

        index = StringUtil.trimLeftWhitespace(source, index2, source.length());
        if (index != source.length()) {
            throw new UnsupportedOperationException("语法错误：未知字符2");
        }

        OpCode begin = OpCode.label();
        OpCode end = OpCode.label();
        list.add(begin);
        Block bnode = (Block) node;
        for (Node sub : bnode.children) {
            sub.getCompiler().compile(sub, list);
        }
        com.diosay.otpl.runtime.opcodes.Block code = new com.diosay.otpl.runtime.opcodes.Block();

        code.setBegin(begin);
        code.setName(name);

        list.add(end);
        //list.add(OpCode.label(node.getLineNumber()));
        list.add(code);
    }

    @Override
    public String getToken() {
        return "section";
    }

    @Override
    public boolean isBlock() {
        return true;
    }

    @Override
    public void parse(Document dom, CharSequence source, int start, int end, int line) {

        Block node = dom.createBlock(this.getToken());
        node.setCompiler(this);
        node.setLineNumber(line);
        node.setSource(source, start, end);
        dom.append(node);
    }

}
