/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package com.diosay.otpl;

/**
 * 表示一个单节点。
 *
 * @author jun <jun@diosay.com>
 */
public class Span extends Node {

    private String name;

    public Span(String name) {
        this.name = name;
    }

    @Override
    public final boolean isBlock() {
        return false;
    }

    @Override
    public final boolean isText() {
        return false;
    }

    @Override
    public final boolean isDocument() {
        return false;
    }

    @Override
    public final boolean isEndBlock() {
        return false;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getNodeType() {
        return Node.SPAN;
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
        this.compiler = compiler;
    }

    /**
     * 设置行号
     *
     * @param line
     */
    public void setLineNumber(int line) {
        this.line = line;
    }

    /**
     * 设置源码
     *
     * @param source
     * @param start
     * @param end
     */
    public void setSource(CharSequence source, int start, int end) {
        for (; start < end && start < source.length(); start++) {
            if (!StringUtil.isWhitespace(source.charAt(start))) {
                break;
            }
        }
        this.source = source.subSequence(start, end);
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
