/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package com.diosay.otpl;

import java.util.LinkedList;
import mano.InvalidOperationException;

/**
 * 表示一个块节点。
 *
 * @author jun <jun@diosay.com>
 */
public class Block extends Node {

    /**
     * 获取当前节点的所有子节点集合。
     */
    public final LinkedList<Node> children = new LinkedList<>();
    private boolean closed;
    private Document dom;
    private CharSequence source;
    private int start;
    private int end;
    private int line;
    private Compiler compiler;
    private String name;

    public Block(Document dom, String name) {
        this.name = name;
        this.dom = dom;
    }

    @Override
    public final boolean isBlock() {
        return true;
    }

    @Override
    public final boolean isText() {
        return false;
    }

    @Override
    public boolean isDocument() {
        return false;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getNodeType() {
        return Node.BLOCK;
    }
    private String[] closes;

    public void setCloseName(String... closes) {
        this.closes = closes;
    }

    /**
     * 使用给定的节点确定是否可以关闭当前节点。
     *
     * @param node
     * @return
     */
    protected boolean canClose(Node node) {
        if (this.getName().equalsIgnoreCase(node.getName()) && node.isEndBlock()) {
            return true;
        } else if (closes != null) {
            for (String s : closes) {
                if (node.getName().equalsIgnoreCase(s)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 向当前节点的dom树附加一个节点。
     *
     * @param node
     * @return
     */
    public Block append(Node node) {
        if (closed) {
            throw new InvalidOperationException("block has been closed.");
        } else if (canClose(node)) {
            closed = true;
            this.dom.current = this.getParent();//fix self-call
            if (node.isEndBlock()) {
                return this.getParent();
            } else {

                node.associate(getParent(), getParent().children.isEmpty() ? null : getParent().children.getLast());
                getParent().children.add(node);
                if (node.isBlock()) {
                    //this.getParent().append(node);
                    return (Block) node;
                } else {
                    return getParent();
                }
            }
        }

        node.associate(this, children.isEmpty() ? null : children.getLast());
        children.add(node);
        if (node.isBlock()) {
            //this.getParent().append(node);
            return (Block) node;
        } else {
            return this;
        }
    }

    @Override
    public Document getOwnerDocument() {
        return dom;
    }

    /**
     * 设置编译器
     *
     * @param compiler
     */
    public void setCompiler(Compiler compiler) {
        this.compiler = compiler;
    }

    @Override
    public Compiler getCompiler() {
        return this.compiler;
    }

    @Override
    public final boolean isEndBlock() {
        return false;
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
        this.source = source.subSequence(start, end);
    }

    @Override
    public CharSequence getSource() {
        return source;
    }

    @Override
    public int getLineNumber() {
        return this.line;
    }
}
