/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package com.diosay.otpl;
//http://msdn.microsoft.com/zh-cn/library/system.xml.xmlnode(v=vs.110).aspx

/**
 * 表示 OTPL 文档中的单个节点。
 *
 * @author jun <jun@diosay.com>
 */
public abstract class Node {

    public static final int UNDEFINED = 0;
    /**
     * 作为文档树的根的文档对象提供对整个 OTPL 文档的访问。
     */
    public static final int DOCUMENT = 1;
    /**
     * 表示一个独立的节点，它不能含有子节点。
     */
    public static final int SPAN = 2;
    /**
     * 表示一个块节点，它必须使用一对<code>{node}{/node}</code> 来指示完整的块。
     */
    public static final int BLOCK = 3;
    /**
     * 表示文本节点。 它是一个特殊的 SPAN 节点，唯一不同是因为它不能被语法解析。
     */
    public static final int TEXT = 4;
    
    /**
     * 表示块节点的结束。
     */
    public static final int END_BLOCK = 5;

    private Block parent;
    private Node prev;
    private Node next;

    /**
     * 获取一个值，以指示当前节点是否是块结点。
     *
     * @return
     */
    public abstract boolean isBlock();

    /**
     * 获取一个值，以指示当前节点是否是文本结点。
     *
     * @return
     */
    public abstract boolean isText();

    /**
     * 获取一个值，以指示当前文档的根类型。
     *
     * @return
     */
    public abstract boolean isDocument();
    
    /**
     * 获取一个值，以指示当前文档的根类型。
     *
     * @return
     */
    public abstract boolean isEndBlock();

    /**
     * 获取当前节点的名称。
     *
     * @return
     */
    public abstract String getName();

    /**
     * 获取当前节点的类型。
     *
     * @return
     */
    public abstract int getNodeType();

    /**
     * 获取该节点（对于可以具有父级的节点）的父级。
     *
     * @return
     */
    public Block getParent() {
        return parent;
    }

    /**
     * 获取紧接在该节点之后的节点。
     *
     * @return
     */
    public final Node getNext() {
        return next;
    }

    /**
     * 获取紧接在该节点之前的节点。
     *
     * @return
     */
    public final Node getPrev() {
        return prev;
    }

    /**
     * 获取所属的文档。
     *
     * @return
     */
    public abstract Document getOwnerDocument();

    /**
     * 获取编译器
     *
     * @return
     */
    public abstract Compiler getCompiler();

    /**
     * 关联节点。
     * <p>
     * 注意：该方法只应在append方法中调用。
     *
     * @param parent 父级
     * @param prev 上个节点
     */
    public final void associate(Block parent, Node prev) {
        this.parent = parent;
        this.prev = prev;
        if (prev != null) {
            prev.next = this;
        }
    }
    
    /**
     * 获取行号
     * @return 
     */
    public abstract int getLineNumber();
    
    /**
     * 获取未解析的源码
     * @return 
     */
    public abstract CharSequence getSource();
    
    /**
     * 一个临时标记
     */
    public boolean marked;
}
