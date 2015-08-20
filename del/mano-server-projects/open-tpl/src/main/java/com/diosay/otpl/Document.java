/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package com.diosay.otpl;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 表示一个文档
 *
 * @author jun <jun@diosay.com>
 */
public class Document extends Block {

    Block current;
    public Node layout;
    public Node body;
    public ArrayList<Node> blocks;
    private Compiler compiler;
    private String file;

    public Document(Compiler compiler, String filename) {
        super(null, "dom");
        this.compiler = compiler;
        this.file = filename;
        this.current = this;
        this.blocks = new ArrayList<>();
    }

    @Override
    public Block append(Node node) {
        if ("layout".equalsIgnoreCase(node.getName())) {
            if (layout != null) {
                throw new mano.InvalidOperationException("layout 标签在行 " + layout.getLineNumber() + " 已经定义，不能重复定义。");
            }
            layout = node;
        } else if ("body".equalsIgnoreCase(node.getName())) {
            if (body != null) {
                throw new mano.InvalidOperationException("body 标签在行 " + body.getLineNumber() + " 已经定义，不能重复定义。");
            }
            body = node;
            if (current != this) {
                current = current.append(node);
            } else {
                current = super.append(node);
            }
        } else if ("section".equalsIgnoreCase(node.getName())) {
            if (!node.isEndBlock()) {
                if (current.isBlock() && "section".equalsIgnoreCase(current.getName())) {
                    throw new mano.InvalidOperationException("section 标签不支持嵌套。");
                }
                node.associate(this, blocks.isEmpty() ? null : blocks.get(blocks.size() - 1));
                blocks.add(node);
                current = (Block) node;
            } else {
                current = current.append(node);
            }
        } else {
            if (current != this) {
                current = current.append(node);
            } else {
                current = super.append(node);
            }
        }
        return current;
    }

    @Override
    protected final boolean canClose(Node node) {
        return false;
    }

    @Override
    public final Compiler getCompiler() {
        return compiler;
    }

    @Override
    public final Document getOwnerDocument() {
        return null;
    }

    @Override
    public final boolean isDocument() {
        return true;
    }

    /**
     * 获取源文件的全限定名称。
     *
     * @return
     */
    public final String getSourceFile() {
        return file;
    }

    /**
     * 创建一个块结束节点。
     *
     * @param name
     * @return
     */
    public EndBlock createEndBlock(String name, int line) {
        return new EndBlock(this, name, line);
    }

    /**
     * 创建一个块节点。
     *
     * @param name
     * @return
     */
    public Block createBlock(String name) {
        return new Block(this, name);
    }

}
