/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package com.diosay.otpl.runtime;

import com.diosay.otpl.Document;
import com.diosay.otpl.Parser;
import com.diosay.otpl.runtime.opcodes.EndOfFile;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import mano.DateTime;
import mano.util.Utility;

/**
 * 实现的 OTPL 解释器
 *
 * @author jun <jun@diosay.com>
 */
public class Interpreter implements Closeable {

    @Override
    public void close() throws IOException {
        //
    }

    static class TempExecutionContext implements ExecutionContext {

        private HashMap<String, Object> items = new HashMap<>();
        private Stack<Object> stack = new Stack<>();

        @Override
        public String getBasedir() {

            return "E:\\repositories\\java\\mano\\mano-server-projects\\otpl4j\\demo";
        }

        @Override
        public String getTempdir() {
            return "C:\\Users\\jun\\Desktop\\demo";
        }

        @Override
        public void set(String key, Object value) {
            items.put(key, value);
        }

        @Override
        public Object get(String key) {
            if (items.containsKey(key)) {
                return items.get(key);
            }
            return null;//TODO:报错？
        }

        @Override
        public void push(Object value) {
            stack.push(value);
        }

        @Override
        public Object pop() {
            if (stack.isEmpty()) {
                return null;
            }
            return stack.pop();
        }

        @Override
        public void reset() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Charset outputEncoding() {
            return Charset.defaultCharset();
//return Charset.forName("utf-8");
        }

        @Override
        public void write(boolean filtrable, Object obj) {
            if (obj == null) {
                return;
            }
            String s = obj.toString();
            write(filtrable, s, 0, s.length());
        }

        @Override
        public void write(boolean filtrable, CharSequence cs, int start, int end) {
            byte[] array = cs.subSequence(start, end).toString().getBytes(this.outputEncoding());
            write(filtrable, array, 0, array.length);
        }

        @Override
        public void write(boolean filtrable, byte[] array, int index, int count) {
            if (filtrable) {
                //TODO:
            }

            System.out.write(array, index, count);
        }

        @Override
        public Charset inputEncoding() {
            return Charset.forName("utf-8");
        }

        @Override
        public Interpreter newInterpreter() {
            return new Interpreter();
        }

        @Override
        public void freeInterpreter(Interpreter interpreter) {

        }

        private HashMap<String, CodeLoader> loaders = new HashMap<>();

        @Override
        public CodeLoader getLoader(File source, Interpreter interpreter) throws Exception {

            String id = Integer.toHexString(source.toString().hashCode());
            if (loaders.containsKey(id)) {
                return loaders.get(id);
            }

            if (interpreter == null) {
                interpreter = newInterpreter();
                loaders.put(id, interpreter.load(this, source));
                freeInterpreter(interpreter);
            } else {
                loaders.put(id, interpreter.load(this, source));
            }
            return loaders.get(id);
        }

        @Override
        public Object peek() {
            return this.stack.peek();
        }
        private int lineNumber;

        @Override
        public void setCurrentSourceLine(int line) {
            lineNumber = line;
        }

        @Override
        public int getCurrentSourceLine() {
            return lineNumber;
        }

        @Override
        public BuiltinFunctionInterface calls() {
            return new BFI();
        }

        @Override
        public Object call(String funcName, Object[] args) {

            if ("str".equals(funcName)) {
                return calls().str(args);
            }else if ("int".equals(funcName)) {
                return Utility.toInt(args[0]);
            }else if ("long".equals(funcName)) {
                return Utility.toLong(args[0]);
            } else if ("iterator".equals(funcName)) {
                return calls().iterator(args[0]);
            } else if ("iterator$hasNext".equals(funcName)) {
                Iterator itor = (Iterator) args[0];
                return itor.hasNext();
            } else if ("iterator$next".equals(funcName)) {
                Iterator itor = (Iterator) args[0];
                return itor.next();
            } else if ("indexer".equals(funcName)) {
                if (args.length < 2) {
                    throw new UnsupportedOperationException("参数不匹配：" + funcName);
                }
                Object[] nargs = new Object[args.length - 1];
                System.arraycopy(args, 0, nargs, 0, args.length - 1);
                return calls().indexer(args[args.length - 1], nargs);
            } else {
                Object obj = items.get(funcName);
                if (obj == null || !(obj instanceof Method)) {
                    throw new UnsupportedOperationException("函数未定义：" + funcName);
                }
                Method method = (Method) obj;
                method.setAccessible(true);
                try {

                    Class<?>[] types = method.getParameterTypes();
                    if (args.length < types.length) {
                        throw new UnsupportedOperationException("参数不匹配：" + funcName);
                    }
                    Object[] nargs = new Object[types.length];
                    for (int i = 0; i < types.length; i++) {
                        Class<?> clazz = types[i];

                        nargs[i] = Utility.cast(clazz, args[i]);
                    }

                    return method.invoke(null, nargs);
                } catch (IllegalAccessException | IllegalArgumentException ex) {
                    throw new RuntimeException(ex);
                } catch (InvocationTargetException ex) {
                    if (ex.getTargetException() != null) {
                        throw new RuntimeException(ex.getTargetException());
                    }
                    throw new RuntimeException(ex);
                }

            }
        }

        static Object invokeMethod(Method method, Object instance, Object[] args) {
            if (method == null) {
                return null;
            }

            try {
                method.setAccessible(true);

                Class<?>[] types = method.getParameterTypes();
                if (args.length < types.length) {
                    throw new UnsupportedOperationException("参数不匹配：" + method.getName());
                }
                Object[] nargs = new Object[types.length];
                for (int i = 0; i < types.length; i++) {
                    Class<?> clazz = types[i];

                    nargs[i] = Utility.cast(clazz, args[i]);
                }

                return method.invoke(instance, nargs);
            } catch (IllegalAccessException | IllegalArgumentException ex) {
                throw new RuntimeException(ex);
            } catch (InvocationTargetException ex) {
                if (ex.getTargetException() != null) {
                    throw new RuntimeException(ex.getTargetException());
                }
                throw new RuntimeException(ex);
            }
        }

        class BFI implements BuiltinFunctionInterface {

            @Override
            public String str(Object[] args) {
                if (args == null) {
                    return null;
                }
                StringBuilder sb = new StringBuilder();
                for (Object obj : args) {
                    sb.append(obj);
                }
                return sb.toString();
            }

            public int len(Object value) {
                if (value == null) {
                    return -1;
                }

                return 0;
            }

            @Override
            public Object iterator(Object obj) {
                if (obj == null) {
                    obj = new Object[0];
                }

                if (obj instanceof Iterable) {
                    return ((Iterable) obj).iterator();
                } else if (obj instanceof Map) {
                    return ((Iterable) ((Map) obj).entrySet()).iterator();
                } else if (obj.getClass().isArray()) {
                    final Object array = obj;
                    final int size = Array.getLength(obj);
                    return new Iterator() {
                        int current = 0;

                        @Override
                        public boolean hasNext() {
                            return current < size;
                        }

                        @Override
                        public Object next() {
                            return Array.get(array, current++);
                        }

                    };
                }
                throw new RuntimeException("give object is a non-iterable object." + obj.getClass());
            }

            @Override
            public Object indexer(Object obj, Object[] args) {

                if (obj == null) {
                    return null;
                } else if (obj.getClass().isArray()) {
                    return Array.get(obj, Integer.parseInt(args[0].toString()));
                } else {
                    Method method = null;
                    for (Method m : obj.getClass().getMethods()) {
                        if (m.getName().equals("get") && m.getParameterCount() == args.length) {
                            method = m;
                            break;
                        }
                    }
                    if (method == null) {
                        throw new UnsupportedOperationException("函数未定义：" + "get");
                    }
                    return invokeMethod(method, obj, args);
                }
            }
        }

    }

    static class MyItem {

        public int name = 0;

        public String getName2() {
            return "hello" + name;
        }
        
        public Object get(Object obj){
            return obj;
        }
    }
    
    static class MyItemXX {
        
        public MyItem inner=new MyItem();

    }

    private static String substr(String n, Integer len) {
        if (n == null || len < 0) {
            return n;
        }
        return n.substring(0, len);
    }

    public static void main2(String[] args) throws Exception {

        Interpreter interpreter = new Interpreter();
        ArrayList<MyItem> list = new ArrayList<>();
        MyItem item = new MyItem();
        item.name = 1;
        list.add(item);
        item = new MyItem();
        item.name = 2;
        list.add(item);
        item = new MyItem();
        item.name = 3;
        list.add(item);
        String file = "E:\\repositories\\java\\mano\\mano-server-projects\\otpl4j\\demo\\page.html";
        TempExecutionContext context = new TempExecutionContext();
        context.items.put("name", "张三");
        context.items.put("list", list);
        context.items.put("item2", new MyItemXX());
        context.items.put("item", new MyItem());
        context.items.put("arr", new String[]{"张三","李四","王二"});
        context.items.put("substr", Interpreter.class.getDeclaredMethod("substr", String.class, Integer.class));
        interpreter.exec(context, new File(file));

    }

    /**
     * 编译一个文件OTPL源文件。
     *
     * @param filename
     */
    public void compileFile(File file, String basedir, File target) throws Exception {

        Parser parser = new Parser();
        Charset encoding = Charset.forName("utf-8");
        try (BufferedReader reader = Parser.open(file.toString())) {
            Document dom = parser.parse(reader, file.toString());
            ArrayList<OpCode> list = new ArrayList<>();
            dom.getCompiler().compile(dom, list);
            list.add(new EndOfFile());
            int line = 0;
            for (OpCode code : list) {
                line++;
                code.setAddress(line);
            }
            target.delete();
            target.createNewFile();
            try (FileOutputStream out = new FileOutputStream(target)) {

                //文件头
                //版本     类型              目标文件最后修改时间 源文件最后修改时间 文件名长度 文件名
                //OTPL-01 1(FILE 1,OTHER:2  {8 LONG}           {8}                {4}        N
                byte[] bytes = file.toString().getBytes(encoding);
                out.write("OTPL-02".getBytes(encoding));
                out.write(1);
                out.write(Utility.toBytes(DateTime.nowTime()));
                out.write(Utility.toBytes(file.lastModified()));
                out.write(Utility.toBytes(bytes.length));
                out.write(bytes);
                for (OpCode code : list) {
                    code.compile(out, encoding);
                }

            }
        }

    }

    /**
     * 载入文件
     *
     * @param context
     * @param source
     * @return
     * @throws Exception
     */
    public CodeLoader load(ExecutionContext context, File source) throws Exception {
        CodeLoader loader = new CodeLoader();
        File target;
        target = new File(Utility.toPath(context.getTempdir(), Integer.toHexString(source.toString().hashCode()) + ".otc").toString());

        boolean compiled = true;
        if (!target.exists() || !target.isFile()) {
            compiled = false;
        } else {
            if (!loader.load(context, new FileInputStream(target), true, source)) {
                compiled = false;
            }
        }

        if (!compiled) {
            try {
                compileFile(source, context.getBasedir(), target);
            } catch (java.io.FileNotFoundException ex) {
                throw new java.io.FileNotFoundException("未找到源文件：" + source.toString());
            }
        }

        if (!loader.load(context, new FileInputStream(target), true, source)) {
            throw new mano.InvalidOperationException("编译失败");
        }

        return loader;
    }

    /**
     * 执行一个OTPL源文件。
     *
     * @param context
     * @param source
     * @throws Exception
     */
    public void exec(ExecutionContext context, File source) throws Exception {
        this.exec(context, context.getLoader(source, this));
    }

    /**
     * 执行已经加载的代码
     *
     * @param context
     * @param loader
     * @throws Exception
     */
    public void exec(ExecutionContext context, CodeLoader loader) throws Exception {
        exec(context,loader,loader.startAddr,loader.endAddr);
//        int next = loader.startAddr;
//        OpCode code;
//        while (true) {
//            if (next <= 0) {
//                break;
//            }
//            code = loader.loadCode(next);
//            if (code == null) {
//                throw new java.lang.RuntimeException("无效的指令地址：" + next);
//            } else if (code.getAddress() == loader.endAddr) {
//                break;
//            }
//            next = code.execute(context);
//            if (code.getType() == OpcodeType.doc) {
//                code.getLoader().parent.pageAddr = next;
//                return;
//            }
//        }

    }

    /**
     * 执行指定段的代码
     *
     * @param context
     * @param loader
     * @param start
     * @param end
     * @throws Exception
     */
    public void exec(ExecutionContext context, CodeLoader loader, int start, int end) throws Exception {
        int next = start < 1 ? loader.startAddr : start;
        int lineNumber = 0;
        OpCode code;
        while (true) {
            if (next <= 0) {
                break;
            }
            if (end != -1 && next >= end) {
                break;
            }

            code = loader.loadCode(next);
            if (code == null) {
                throw new java.lang.RuntimeException("无效的指令地址：" + next);
            }
            else if (code.getAddress() == loader.endAddr) {
                break;
            }
            next = code.execute(context);
            lineNumber = context.getCurrentSourceLine();
            if (code.getType() == OpcodeType.doc) {
                code.getLoader().parent.pageAddr = next;
                return;
            }
        }
    }
}
