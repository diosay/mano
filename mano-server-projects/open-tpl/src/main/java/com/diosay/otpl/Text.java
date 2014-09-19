/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */

package com.diosay.otpl;

/**
 * 纯文本。
 * @author jun <jun@diosay.com>
 */
public class Text extends Node {

    public Text(Document dom,int line){
        this.dom=dom;
        this.line=line;
    }
    
    @Override
    public boolean isBlock() {
        return false;
    }

    @Override
    public boolean isText() {
        return true;
    }

    @Override
    public boolean isDocument() {
        return false;
    }

    @Override
    public boolean isEndBlock() {
        return false;
    }

    @Override
    public String getName() {
        return "text";
    }

    @Override
    public int getNodeType() {
        return Node.TEXT;
    }

    @Override
    public Document getOwnerDocument() {
        return dom;
    }

    @Override
    public Compiler getCompiler() {
        return compiler;
    }
    private Compiler compiler;
    private Document dom;
    private CharSequence source;
    private int start;
    private int end;
    private int line;
    /**
     * 设置编译器
     *
     * @param compiler
     */
    public void setCompiler(Compiler compiler) {
        this.compiler=compiler;
    }
    /**
     * 设置行号
     * @param line 
     */
    public void setLineNumber(int line){
        this.line=line;
    }
    /**
     * 设置源码
     * @param source
     * @param start
     * @param end 
     */
    public void setSource(CharSequence source, int start, int end){
        this.source=source.subSequence(start, end);
    }

    @Override
    public int getLineNumber() {
        return this.line;
    }

    @Override
    public CharSequence getSource() {
        return this.source;
    }
}
