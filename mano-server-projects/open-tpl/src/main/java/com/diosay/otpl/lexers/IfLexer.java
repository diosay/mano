/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package com.diosay.otpl.lexers;

import com.diosay.otpl.Block;
import com.diosay.otpl.CompileException;
import com.diosay.otpl.Compiler;
import com.diosay.otpl.Document;
import com.diosay.otpl.Lexer;
import com.diosay.otpl.Node;
import com.diosay.otpl.runtime.OpCode;
import java.util.ArrayList;

/**
 * 解析与编译if语句
 *
 * @author jun <jun@diosay.com>
 */
public class IfLexer extends Compiler implements Lexer {

    @Override
    public String getToken() {
        return "if";
    }

    @Override
    public boolean isBlock() {
        return true;
    }

    @Override
    public void parse(Document dom, CharSequence source, int start, int end, int line) {
        if (start >= end) {
            return;
        }
        Block node = dom.createBlock(this.getToken());
        node.setCompiler(this);
        node.setSource(source, start, end);
        node.setLineNumber(line);
        node.setCloseName("elif", "else");
        dom.append(node);
    }

    @Override
    public void compile(Node node, ArrayList<OpCode> list) {

        // <editor-fold defaultstate="collapsed" desc="IL Code">
        /*
         if(3<2){
         
         }
        
         ldu 3
         ldu 2
         lt
         brf RET
         ...
         RET
         
         */
        // </editor-fold>
        if (node == null || !node.isBlock() || !this.getToken().equalsIgnoreCase(node.getName())) {
            throw new IllegalArgumentException("node");
        }

        OpCode lbElse = OpCode.label();
        OpCode ret = OpCode.label();
        this.compile(node.getSource(), list, node.getLineNumber());
        list.add(OpCode.makeBreakFalse(lbElse));

        ((Block) node).children.stream().forEach((sub) -> {
            sub.getCompiler().compile(sub, list);
        });
        list.add(OpCode.makeBreak(ret));
        list.add(lbElse);
        do {
            Node next = node.getNext();
            if (next == null) {
                break;
            } else if ("elif".equals(next.getName())) {

                if (!("if".equals(next.getPrev().getName()) || "elif".equals(next.getPrev().getName()))) {
                    throw new CompileException("语法错误");
                }
                OpCode lbElse2 = OpCode.label();
                this.compile(next.getSource(), list, next.getLineNumber());
                list.add(OpCode.makeBreakFalse(lbElse2));
                ((Block) next).children.stream().forEach((sub) -> {
                    sub.getCompiler().compile(sub, list);
                });
                list.add(OpCode.makeBreak(ret));
                list.add(lbElse2);
            } else if ("else".equals(next.getName())) {
                if (!("if".equals(next.getPrev().getName()) || "elif".equals(next.getPrev().getName()))) {
                    throw new CompileException("语法错误");
                }
                ((Block) next).children.stream().forEach((sub) -> {
                    sub.getCompiler().compile(sub, list);
                });
                list.add(OpCode.makeBreak(ret));
            } else {
                break;
            }
            next.marked = true;
            node = next;
        } while (true);
        list.add(ret);
    }
}
