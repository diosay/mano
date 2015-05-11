/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package com.diosay.otpl.runtime.opcodes;

import com.diosay.otpl.runtime.ExecutionContext;
import com.diosay.otpl.runtime.OpCode;
import com.diosay.otpl.runtime.OpcodeType;
import java.io.OutputStream;
import java.nio.charset.Charset;
import mano.util.Utility;

/**
 * 操作符
 *
 * @author jun <jun@diosay.com>
 */
public class Operator extends OpCode {

    public static final byte /**
             * 加
             */
            ADD = 1,
            /**
             * 减
             */
            SUB = 2,
            /**
             * 乘
             */
            MUL = 3,
            /**
             * 除
             */
            DIV = 4,
            /**
             * 模
             */
            MOD = 5,
            /**
             * 等于
             */
            EQ = 6,
            /**
             * 不等于
             */
            NE = 7,
            /**
             * 大于
             */
            GT = 8,
            /**
             * 大于等于
             */
            GE = 9,
            /**
             * 小于
             */
            LT = 10,
            /**
             * 小于等于
             */
            LE = 11,
            /**
             * 与
             */
            AND = 12,
            /**
             * 或
             */
            OR = 13,
            /**
             * 非
             */
            NOT = 14,
            /**
             * 负
             */
            NEG = 15;

    private byte operator;
    public Operator setOperator(byte op){
        this.operator=op;
        return this;
    }
    public static Operator parse(byte operator) {
        switch (operator) {
            case Operator.ADD:
            case Operator.AND:
            case Operator.DIV:
            case Operator.EQ:
            case Operator.GE:
            case Operator.GT:
            case Operator.LE:
            case Operator.LT:
            case Operator.MOD:
            case Operator.MUL:
            case Operator.NE:
            case Operator.NOT:
            case Operator.OR:
            case Operator.SUB:
                break;
        }
        return null;
    }

    @Override
    public OpcodeType getType() {
        return OpcodeType.op;
    }

    @Override
    public void compile(OutputStream output, Charset encoding) throws Exception {
        output.write(Utility.toBytes(this.getAddress()));
        output.write(this.getType().value);
        output.write(operator);
    }

    private boolean toBool(Object obj) {
        if (obj == null) {
            return false;
        }

        try {
            return Utility.cast(Boolean.class, obj);
        } catch (Exception ex) {
            try {
                return Utility.cast(Double.class, obj) == 0 ? false : true;
            } catch (Exception ex2) {
                return true;
            }
        }
    }

    @Override
    public int execute(ExecutionContext context) throws Exception {

        switch (this.operator) {
            case Operator.ADD: {
                Object left = context.pop();
                Object right = context.pop();
                context.push(Utility.asNumber(Math.max(Utility.geTypeCode(left.getClass()),
                        Utility.geTypeCode(right.getClass())),
                        Utility.toDouble(left) + Utility.toDouble(right)));
                break;
            }
            case Operator.AND: {
                context.push(this.toBool(context.pop()) && this.toBool(context.pop()));
                break;
            }
            case Operator.DIV: {
                Object left = context.pop();
                Object right = context.pop();
                context.push(Utility.asNumber(Math.max(Utility.geTypeCode(left.getClass()),
                        Utility.geTypeCode(right.getClass())),
                        Utility.toDouble(left) / Utility.toDouble(right)));
                break;
            }
            case Operator.EQ: {
                Object left = context.pop();
                Object right = context.pop();
                if (left != null) {
                    context.push(left.equals(right));
                } else if (right != null) {
                    context.push(right.equals(left));
                } else {
                    context.push(right == left);
                }
                break;
            }
            case Operator.GE: {
                Object right = context.pop();
                Object left = context.pop();
                context.push(Utility.toDouble(left) >= Utility.toDouble(right));
                break;
            }
            case Operator.GT:{
                Object right = context.pop();
                Object left = context.pop();
                context.push(Utility.toDouble(left) > Utility.toDouble(right));
                break;
            }
            case Operator.LE:{
                Object right = context.pop();
                Object left = context.pop();
                context.push(Utility.toDouble(left) <= Utility.toDouble(right));
                break;
            }
            case Operator.LT:{
                Object right = context.pop();
                Object left = context.pop();
                context.push(Utility.toDouble(left) < Utility.toDouble(right));
                break;
            }
            case Operator.MOD: {
                Object left = context.pop();
                Object right = context.pop();
                context.push(Utility.asNumber(Math.max(Utility.geTypeCode(left.getClass()),
                        Utility.geTypeCode(right.getClass())),
                        Utility.toDouble(left) % Utility.toDouble(right)));
                break;
            }
            case Operator.MUL: {
                Object left = context.pop();
                Object right = context.pop();
                context.push(Utility.asNumber(Math.max(Utility.geTypeCode(left.getClass()),
                        Utility.geTypeCode(right.getClass())),
                        Utility.toDouble(left) * Utility.toDouble(right)));
                break;
            }
            case Operator.NE: {
                Object left = context.pop();
                Object right = context.pop();
                if (left != null) {
                    context.push(!left.equals(right));
                } else if (right != null) {
                    context.push(!right.equals(left));
                } else {
                    context.push(right != left);
                }
                break;
            }
            case Operator.NOT:{
                context.push(!this.toBool(context.pop()));
                break;
            }case Operator.NEG:{
                String obj=context.pop().toString();
                if(obj.indexOf('.')>0){
                    context.push(-Utility.toDouble(obj));
                }else{
                    context.push(-Utility.toLong(obj));
                }
                
                break;
            }
            case Operator.OR: {
                context.push(this.toBool(context.pop()) || this.toBool(context.pop()));
                break;
            }
            case Operator.SUB: {
                Object right = context.pop();
                Object left = context.pop();
                context.push(Utility.asNumber(Math.max(Utility.geTypeCode(left.getClass()),
                        Utility.geTypeCode(right.getClass())),
                        Utility.toDouble(left) - Utility.toDouble(right)));
                break;
            }
        }

        return this.getAddress() + 1;
    }

}
