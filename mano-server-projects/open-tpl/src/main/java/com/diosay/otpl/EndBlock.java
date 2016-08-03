/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package com.diosay.otpl;

import com.diosay.otpl.runtime.OpCode;
import java.util.ArrayList;

/**
 * 表示一个块的结束。
 *
 * @author jun <jun@diosay.com>
 */
public class EndBlock extends Node {

    private String name;
    private Document dom;
    private int line;

    protected EndBlock() {
    }

    EndBlock(Document dom, String name, int line) {
        this.dom = dom;
        this.name = name;
        this.line = line;
    }

    @Override
    public boolean isBlock() {
        return false;
    }

    @Override
    public boolean isText() {
        return false;
    }

    @Override
    public boolean isDocument() {
        return false;
    }

    @Override
    public final boolean isEndBlock() {
        return true;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public final int getNodeType() {
        return Node.END_BLOCK;
    }

    @Override
    public Document getOwnerDocument() {
        return dom;
    }

    @Override
    public Compiler getCompiler() {
        return dc;
    }
    static cc dc = new cc();

    static class cc extends com.diosay.otpl.Compiler {

        @Override
        public void compile(Node node, ArrayList<OpCode> list) {
            System.out.println("WARNING: 结束标签不应该被编译。name:" + node != null ? node.getName() : "");
        }

    }

    @Override
    public int getLineNumber() {
        return line;
    }

    @Override
    public CharSequence getSource() {
        return "";
    }
}
