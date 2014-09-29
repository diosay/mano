/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.expressions;
//http://msdn.microsoft.com/zh-cn/library/system.linq.expressions(v=vs.110).aspx
//http://msdn.microsoft.com/zh-cn/library/system.linq.expressions.expression(v=vs.110).aspx

import mano.Value;


/**
 * 表示一个表达式基类。
 *
 * @author jun <jun@diosay.com>
 */
public abstract class Expression {

    /**
     * 获取此 Expression 的节点类型。
     * @return 
     */
    public abstract ExpressionType getNodeType();
    
    
    static Value<Integer> v = Value.wrap(1);
    static void set(Value<Integer> val){
        val.set(10);
    }
    
    
    public static void mainxx(String[] args) {
        System.out.println(""+v.get());
        set(v);
        
        System.out.println(""+v.get());
//        for (ExpressionType type : ExpressionType.values()) {
//            System.out.println(""+type.name());
//        }
    }
}
