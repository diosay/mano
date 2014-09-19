/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package com.diosay.otpl;

/**
 * 表示一个块的结束。
 *
 * @author jun <jun@diosay.com>
 */
public class EndBlock extends Node {
    private String name;
    private Document dom;
    protected EndBlock() {
    }

    EndBlock(Document dom, String name, int line) {
        this.dom=dom;
        this.name=name;
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Compiler getCompiler() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int getLineNumber() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CharSequence getSource() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
