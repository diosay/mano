/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.expressions;

/**
 * 表示表达式树的访问者或重写者。
 * @author jun <jun@diosay.com>
 */
public interface ExpressionVisitor {
    /**
     * 将表达式调度到此类中更专用的访问方法之一。
     * @param node 
     */
    void visit(Expression node);
}
