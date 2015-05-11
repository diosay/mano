/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package com.diosay.otpl.runtime.opcodes;

import com.diosay.otpl.CompileException;
import com.diosay.otpl.runtime.ExecutionContext;
import com.diosay.otpl.runtime.OpCode;
import com.diosay.otpl.runtime.OpcodeType;
import com.diosay.otpl.runtime.Util;
import java.io.OutputStream;
import java.nio.charset.Charset;
import mano.util.Utility;

/**
 * 无条件地将控制转移到目标指令。
 *
 * @author jun <jun@diosay.com>
 */
public class Break extends OpCode {

    private byte behavior;
    private int addr = -1;
    private OpCode target;

    public static final byte BREAK = 0;
    public static final byte BREAK_FALSE = 1;
    public static final byte BREAK_TRUE = 2;
    public static final byte BREAK_EXIT = 3;

    /**
     * 设置跳转目标。
     *
     * @param addr
     * @return
     */
    public Break setTarget(OpCode addr) {
        if (addr == null) {
            throw new IllegalArgumentException();
        }
        target = addr;
        return this;
    }

    /**
     * 设置跳转目标。
     *
     * @param addr
     * @return
     */
    public Break setTarget(int target) {
        if (target <= 0) {
            throw new IllegalArgumentException();
        }
        addr = target;
        return this;
    }

    public Break setBehavior(byte b) {
        behavior = b;
        return this;
    }

    public byte getBehavior() {
        return behavior;
    }

    @Override
    public OpcodeType getType() {
        return OpcodeType.br;
    }

    @Override
    public void compile(OutputStream output, Charset encoding) throws Exception {
        output.write(Utility.toBytes(this.getAddress()));
        output.write(this.getType().value);
        output.write(behavior);
        if (behavior != BREAK_EXIT) {
            if (addr <= -1) {
                if (target == null) {
                    throw new CompileException("未设置目标地址。");
                }
                addr = target.getAddress();
            }
            output.write(Utility.toBytes(addr));
        }
    }

    @Override
    public int execute(ExecutionContext context) throws Exception {
        if (behavior != BREAK_EXIT) {
            if (addr <= -1) {
                if (target == null) {
                    throw new RuntimeException("未设置目标地址。");
                }
                addr = target.getAddress();
            }
            if (behavior == BREAK) {
                return addr;
            } else if (behavior == BREAK_TRUE || behavior == BREAK_FALSE) {
                boolean b=Util.toBoolean(context.pop());
                if (behavior == BREAK_FALSE && !b) {
                    return addr;
                } else if (behavior == BREAK_TRUE && b) {
                    return addr;
                }
            }

            return this.getNextAddress();
        }
        return -1;
    }

}
