/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.expressions;

/**
 * 读取变量表达式
 * @author jun <jun@diosay.com>
 */
public class VariableAccessExpression extends Expression {

    @Override
    public ExpressionType getNodeType() {
        return ExpressionType.VariableAccess;
    }
    
}
