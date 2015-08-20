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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import mano.util.Utility;

/**
 * 调用一个函数。
 *
 * @author jun <jun@diosay.com>
 */
public class Call extends OpCode {

    private int len;

    public Call setArgLength(int n) {
        len = n;
        return this;
    }

    @Override
    public OpcodeType getType() {
        return OpcodeType.call;
    }

    @Override
    public void compile(OutputStream output, Charset encoding) throws Exception {
        output.write(Utility.toBytes(this.getAddress()));
        output.write(this.getType().value);
        output.write(Utility.toBytes(this.len));
    }

    @Override
    public int execute(ExecutionContext context) throws Exception {

        ArrayList<Object> args = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            args.add(context.pop());
        }
        Collections.reverse(args);
        Object obj = context.pop();
        Object instance = context.pop();
        if (instance == null) {
            throw new RuntimeException("对象实例不能为 null");
        }
        if (obj == null) {
            throw new UnsupportedOperationException("Null is not a callable object.");
        } else if (obj instanceof Method) {
            Method method = (Method) obj;
            method.setAccessible(true);
            try {

                Class<?>[] types = method.getParameterTypes();
                if (args.size() != types.length) {
                    throw new UnsupportedOperationException("参数不匹配：" + len);
                }
                Object[] nargs = new Object[types.length];
                for (int i = 0; i < types.length; i++) {
                    Class<?> clazz = types[i];
                    nargs[i] = Utility.cast(clazz, args.get(i));
                }
                context.push(method.invoke(instance, nargs));
            } catch (IllegalAccessException | IllegalArgumentException ex) {
                throw new RuntimeException(ex);
            } catch (InvocationTargetException ex) {
                if (ex.getTargetException() != null) {
                    throw new RuntimeException(ex.getTargetException());
                }
                throw new RuntimeException(ex);
            }

        } else if (obj instanceof Field) {
            Field field = (Field) obj;
            field.setAccessible(true);
            context.push(field.get(instance));
        } else {
            throw new UnsupportedOperationException(obj.getClass() + " is not a callable object.");
        }
        
        return this.getNextAddress();
    }

}
