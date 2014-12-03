/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */

package com.diosay.otpl.runtime;

/**
 * 操作码类型
 * @author jun <jun@diosay.com>
 */
public enum OpcodeType {
    /**
     * 表示一个占位，不执行任何操作。通常用作 label 使用。
     */
    nop(0),
    /**
     * 表示一个文档，执行此操作时将打开该文档为执行。
     */
    doc(1),
    /**
     * 从栈顶弹出一个元素并打印到输出流。
     */
    prt(2),
    /**
     * 无条件跳转到指定指令行。
     */
    br(3),
    /**
     * 从栈顶弹出一个元素，如果该对象为假(null,0,false)，则跳转到指定指令行。
     */
    brf(4),
    /**
     * 从栈顶弹出一个元素，并丢弃。
     */
    pop(5),
    /**
     * 根据给定索引，从变量组中获取一个对象，并将该对象压入栈顶。
     */
    ldv(6),
    /**
     * 从栈顶弹出一个对象O,并从中获取其成员对象(字段、属性、方法)，并将对象O与成员对象压入栈顶。
     */
    ldm(7),
    /**
     * 从栈顶弹出一个对象并将该对象设置到变量组中。
     */
    stv(8),
    /**
     * 从栈顶开始弹出执行方法所需对象并执行，将执行结果对象压入栈顶。
     */
    call(9),
    /**
     * 载入一个 double 小数，并压入栈顶。
     */
    ldr(10),
    /**
     * 载入一个 long 整数，并压入栈顶。
     */
    ldu(11),
    /**
     * 载入一个字符串，并压入栈顶。
     */
    ldstr(12),
    /**
     * 从栈顶弹出两个对象进行相加，并将结果压入栈顶。
     */
    @Deprecated
    add(13),
    /**
     * 从栈顶弹出两个对象进行相减，并将结果压入栈顶。
     */
    @Deprecated
    sub(14),
    /**
     * 从栈顶弹出两个对象进行相乘，并将结果压入栈顶。
     */
    @Deprecated
    mul(15),
    /**
     * 从栈顶弹出两个对象进行相除，并将结果压入栈顶。
     */
    @Deprecated
    div(16),
    /**
     * 从栈顶弹出两个对象进行模运算，并将结果压入栈顶。
     */
    @Deprecated
    mod(17),
    /**
     * 从栈顶弹出两个对象进行等于比较，并将结果压入栈顶。
     */
    @Deprecated
    eq(18),
    /**
     * 从栈顶弹出两个对象进行不等于比较，并将结果压入栈顶。
     */
    @Deprecated
    neq(19),
    /**
     * 从栈顶弹出两个对象进行大于比较，并将结果压入栈顶。
     */
    @Deprecated
    gt(20),
    /**
     * 从栈顶弹出两个对象进行大于等于比较，并将结果压入栈顶。
     */
    @Deprecated
    gte(21),
    /**
     * 从栈顶弹出两个对象进行小于比较，并将结果压入栈顶。
     */
    @Deprecated
    lt(22),
    /**
     * 从栈顶弹出两个对象进行小于比较，并将结果压入栈顶。
     */
    @Deprecated
    lte(23),
    /**
     * 从栈顶弹出一个对象进行非运算，并将结果压入栈顶。
     */
    @Deprecated
    not(24),
    /**
     * 从栈顶弹出两个对象进行逻辑与运算，并将结果压入栈顶。
     */
    @Deprecated
    and(25),
    /**
     * 从栈顶弹出两个对象进行逻辑非运算，并将结果压入栈顶。
     */
    @Deprecated
    or(26),
    /**
     * 终止当前文档的执行。
     */
    abort(27),
    /**
     * 结束整个当前事务的执行。
     */
    exit(28),
    /**
     * 头部结束。
     */
    ehead(29),
    /**
     * 设置块变
     */
    stblk(30),
    /**
     * 打印字符串
     */
    ptstr(31),
    /**
     * 正文
     */
    body(32),
    /**
     * 调用一个块。
     */
    call_blk(33),
    /**
     * 压入 null 到栈。
     */
    @Deprecated
    nil(34),
    /**
     * 操作符
     */
    op(35),
    /**
     * 获取栈顶元素，再压入栈顶？
     */
    peek(36),
    /**
     * 临时
     */
    brd(37),
    /**
     * 载入一个预定义常量。
     */
    ldc(38),
    /**
     * 行号
     */
    sl(39),
    callvri(40),
    inc(41);
    
    
    
    public final byte value;
    private OpcodeType(int value){
        this.value=(byte)value;
    }
    
    public static OpcodeType parse(byte[] buffer,int index){
        for (OpcodeType type : OpcodeType.values()) {
            if (buffer[index] == type.value) {
                return type;
            }
        }
        throw new mano.InvalidOperationException("解析失败,value:"+buffer[index]);
    }
}
