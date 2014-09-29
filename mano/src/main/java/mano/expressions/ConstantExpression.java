/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.expressions;

/**
 * 常量表达式
 * @author jun <jun@diosay.com>
 */
public class ConstantExpression extends Expression {

    public ConstantExpression(Object value,int datatype){
        
    }
    
    @Override
    public ExpressionType getNodeType() {
        return ExpressionType.Constant;
    }
    
}
