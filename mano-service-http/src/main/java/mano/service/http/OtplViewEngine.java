package mano.service.http;

import com.diosay.otpl.runtime.BuiltinFunctionInterface;
import com.diosay.otpl.runtime.CodeLoader;
import com.diosay.otpl.runtime.ExecutionContext;
import com.diosay.otpl.runtime.Interpreter;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import mano.net.http.HttpContext;
import mano.util.Utility;
import mano.web.ViewContext;
import mano.web.ViewEngine;

/**
 *
 * @author jun
 */


public class OtplViewEngine extends ViewEngine {
    private Interpreter interpreter = new Interpreter();

    class SimpleExecutionContext extends ViewContext implements ExecutionContext {

        //private HashMap<String, Object> items = new HashMap<>();
        private Stack<Object> stack = new Stack<>();
        private HashMap<String, CodeLoader> loaders = new HashMap<>();

        public SimpleExecutionContext(HttpContext c) {
            super(c);
        }

        @Override
        public String getBasedir() {
            return getViewdir();
            //return "E:\\repositories\\java\\mano\\mano-server-projects\\otpl4j\\demo";
        }

        @Override
        public String getTempdir() {
            return OtplViewEngine.this.getTempdir();
            //return "C:\\Users\\jun\\Desktop\\demo";
        }

//        @Override
//        public void set(String key, Object value) {
//            items.put(key, value);
//        }
//
//        @Override
//        public Object get(String key) {
//            if (items.containsKey(key)) {
//                return items.get(key);
//            }
//            return null;//TODO:报错？
//        }
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
            return this.getEncoding();
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
            this.getContext().getResponse().write(array, index, count);
            //System.out.write(array, index, count);
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
        private BuiltinFunctionInterface bfi;

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
            if (bfi == null) {
                bfi = new BFI();
            }
            return bfi;
        }

        @Override
        public Object call(String funcName, Object[] args) {

            if ("str".equals(funcName)) {
                return calls().str(args);
            } else if ("len".equals(funcName)) {
                if (args == null || args.length == 0 || args[0] == null) {
                    return -1;
                } else if (args[0] instanceof CharSequence) {
                    return ((CharSequence) args[0]).length();
                } else if (args[0].getClass().isArray()) {
                    return Array.getLength(args[0]);
                } else if (args[0] instanceof Collection) {
                    return ((Collection) args[0]).size();
                } else if (args[0] instanceof Map) {
                    return ((Map) args[0]).size();
                }
                return -1;
            } else if ("iterator".equals(funcName)) {
                return calls().iterator(args[0]);
            } else if ("iterator$hasNext".equals(funcName)) {
                Iterator itor = (Iterator) args[0];
                return itor.hasNext();
            } else if ("iterator$next".equals(funcName)) {
                Iterator itor = (Iterator) args[0];
                return !itor.hasNext()?null:itor.next();
            } else if ("indexer".equals(funcName)) {
                if (args.length < 2) {
                    throw new UnsupportedOperationException("参数不匹配：" + funcName);
                }
                Object[] nargs = new Object[args.length - 1];
                System.arraycopy(args, 0, nargs, 0, args.length - 1);
                return calls().indexer(args[args.length - 1], nargs);
            } else {
                Object obj = get(funcName);
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

        Object invokeMethod(Method method, Object instance, Object[] args) {
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

    @Override
    public ViewContext createContext(HttpContext context) {
        return new SimpleExecutionContext(context);
    }
    private void printRoot(StringWriter sb, Throwable t) {
        if (t == null) {
            return;
        }
        sb.append("<b>root</b><p><pre>");
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            pw.println();
            t.printStackTrace(pw);
        }
        sb.append(sw.toString());
        sb.append("</pre></p>");
        printRoot(sb, t.getCause());
    }
    @Override
    public void render(ViewContext context) {
        try {
            String file = context.getPath();
            if (file.startsWith("~/") || file.startsWith("~\\")) {
                file = Utility.toPath(this.getViewdir(), file.substring(1)).toString();
            }
            interpreter.exec((ExecutionContext) context, new File(file));
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            sw.append("<pre>");
            try (PrintWriter pw = new PrintWriter(sw)) {
                pw.println();
                ex.printStackTrace(pw);
            }
            sw.append("</pre>");
            printRoot(sw,ex.getCause());
            context.getContext().getResponse().write(sw.toString());
        }
    }
}
