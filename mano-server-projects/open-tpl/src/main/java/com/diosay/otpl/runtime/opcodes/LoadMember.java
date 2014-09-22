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
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import mano.util.Utility;

/**
 * 载入一个成员方法。
 *
 * @author jun <jun@diosay.com>
 */
public class LoadMember extends OpCode {

    private String name;
    private int len;

    public LoadMember setName(String n) {
        name = n;
        return this;
    }

    public LoadMember setArgLength(int n) {
        len = n;
        return this;
    }

    @Override
    public OpcodeType getType() {
        return OpcodeType.ldm;
    }

    @Override
    public void compile(OutputStream output, Charset encoding) throws Exception {
        output.write(Utility.toBytes(this.getAddress()));
        output.write(this.getType().value);
        output.write(Utility.toBytes(this.len));
        if (this.name != null) {
            byte[] bytes = this.name.getBytes(encoding);
            output.write(Utility.toBytes(bytes.length));
            output.write(bytes);
        } else {
            throw new UnsupportedOperationException("未设置函数名.");
        }
    }

    @Override
    public int execute(ExecutionContext context) throws Exception {

        Object obj = context.pop();
        if (obj == null) {
            context.push(null);
            return this.getNextAddress();
        }
        Class<?> clazz = obj.getClass();
        Member member = null;
        try {
            for (Method m : clazz.getMethods()) {
                if (m.getName().equalsIgnoreCase(name) && m.getParameterCount() == len) {
                    member = m;
                    break;
                }
            }
        } catch (Exception ex) {
        }

        if (member == null) {
            StringBuilder sb = new StringBuilder(name);
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
            sb.insert(0, "get");//getXxxx
            String tmp = sb.toString();
            try {
                for (Method m : clazz.getMethods()) {
                    if (m.getName().equalsIgnoreCase(tmp) && m.getParameterCount() == len) {
                        member = m;
                        break;
                    }
                }
            } catch (Exception ex) {
            }
        }

        if (member == null && len == 0) {
            try {
                for (Field m : clazz.getFields()) {
                    if (m.getName().equalsIgnoreCase(name)) {
                        member = m;
                        break;
                    }
                }
            } catch (Exception ex) {
            }
        }

        if (member == null) {
            throw new UnsupportedOperationException("指定类成员未找到或未定义：" + name);
        }
        context.push(member);

        return this.getNextAddress();
    }

}
