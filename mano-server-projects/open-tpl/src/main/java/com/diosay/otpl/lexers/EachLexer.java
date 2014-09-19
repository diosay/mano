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
import com.diosay.otpl.Token;
import com.diosay.otpl.runtime.OpCode;
import com.diosay.otpl.runtime.opcodes.Callvri;
import com.diosay.otpl.runtime.opcodes.LoadVariable;
import com.diosay.otpl.runtime.opcodes.SetVariable;
import com.diosay.otpl.runtime.opcodes.SourceLineNumber;
import java.util.ArrayList;

/**
 * 列出集合元素
 *
 * @author jun <jun@diosay.com>
 */
public class EachLexer extends com.diosay.otpl.Compiler implements Lexer {

    @Override
    public void compile(Node node, ArrayList<OpCode> rlist) {
        // <editor-fold defaultstate="collapsed" desc="IL Code">
        /*
         for(Object item:list){
         break
         }
        
         loadItear
         stv n
         ldv n
         callvri hsnext
         brf ELSE
         BEGIN
         ldv n
         callvri current
         stv item
         ...
         ldv n
         callvri hsnext
         brf RET
         br BEGIN
         ELSE
         ...
         RET
         
         
         */
        // </editor-fold>

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

        if (this.isKeyword(name)) {
            throw new UnsupportedOperationException("语法错误：变量名不能是关键词：" + name);
        }

        ArrayList<Token> tokens = this.scan(source, index2, source.length(), node.getLineNumber());
        this.grammar(tokens);
        if (tokens.size() != 1) {
            throw new UnsupportedOperationException("语法错误：each语句参数错误。");
        }
        rlist.add(new SourceLineNumber().setValue(node.getLineNumber()));

        for (Token sub : tokens) {
            this.visit(sub, rlist);
        }
        ArrayList<OpCode> list = new ArrayList<>();
        OpCode lbElse = OpCode.label();
        OpCode begin = OpCode.label();
        OpCode ret = OpCode.label();
        begin.tag = "each";
        begin.mark = randomName();
        ret.tag = begin.tag;
        ret.mark = begin.mark;

        SetVariable coll = new SetVariable().setName(randomName());
        //SetVariable var = new SetVariable().setName(name);
        list.add(new Callvri().setName("iterator").setArgLength(1));
        list.add(coll);
        list.add(new LoadVariable().setName(coll.getName()));
        list.add(new Callvri().setName("iterator$hasNext").setArgLength(1));
        list.add(OpCode.makeBreakFalse(lbElse));
        list.add(begin);
        list.add(new LoadVariable().setName(coll.getName()));
        list.add(new Callvri().setName("iterator$next").setArgLength(1));
        list.add(new SetVariable().setName(name));
        ((Block) node).children.stream().forEach((sub) -> {
            sub.getCompiler().compile(sub, list);
        });
        list.add(new LoadVariable().setName(coll.getName()));
        list.add(new Callvri().setName("iterator$hasNext").setArgLength(1));
        list.add(OpCode.makeBreakFalse(ret));
        list.add(OpCode.makeBreak(begin));
        list.add(lbElse);
        //else
        Node next = node.getNext();//is null
        if (next != null && "else".equals(next.getName())) {
            next.marked = true;
            ((Block) next).children.stream().forEach((sub) -> {
                sub.getCompiler().compile(sub, list);
            });
            list.add(OpCode.makeBreak(ret));
        }
        list.add(ret);
        this.adjustEach(list, 0, list.size());
        rlist.addAll(list);
    }

    @Override
    public String getToken() {
        return "each";
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
        node.setCloseName("else");
        dom.append(node);
    }

}
